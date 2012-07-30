package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import static pl.net.bluesoft.rnd.util.TaskUtil.getTaskLink;
import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProviderParams;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.DatabaseMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.JndiMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.templates.MailTemplateProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.rnd.util.i18n.impl.DefaultI18NSource;
import pl.net.bluesoft.util.lang.Strings;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class BpmNotificationEngine implements BpmNotificationService 
{
    private static final String SUBJECT_TEMPLATE_SUFFIX = "_subject";
    private static final String PROVIDER_TYPE = "mail.settings.provider.type";

    private Logger logger = Logger.getLogger(BpmNotificationEngine.class.getName());

    private I18NSource messageSource = new DefaultI18NSource();

    private Collection<BpmNotificationConfig> configCache = new HashSet<BpmNotificationConfig>();

    private long cacheUpdateTime;
    private static final long CONFIG_CACHE_REFRESH_INTERVAL = 60 * 1000;
    private ProcessToolBpmSession bpmSession;

	private final Set<TemplateArgumentProvider> argumentProviders = new HashSet<TemplateArgumentProvider>();

    private Map<String, Properties> persistentMailProperties = new HashMap<String, Properties>();
    
    private IMailSessionProvider mailSessionProvider;
    private MailTemplateProvider templateProvider;
    
    public BpmNotificationEngine()
    {
    	/* Register simple providers */
    	templateProvider = new  MailTemplateProvider();
    	mailSessionProvider = new DatabaseMailSessionProvider();
    }

    public void onProcessStateChange(BpmTask task, ProcessInstance pi, UserData userData, boolean processStarted) {
        refreshConfigIfNecessary();
        for (BpmNotificationConfig cfg : configCache) {
            try {
                if (hasText(cfg.getProcessTypeRegex()) && !pi.getDefinitionName().matches(cfg.getProcessTypeRegex())) {
                    continue;
                }
                if (!(
					(!hasText(cfg.getStateRegex()) || (task != null && task.getTaskName().matches(cfg.getStateRegex())))
					||
					(cfg.isNotifyOnProcessStart() && processStarted)
				)) {
                    continue;
                }
                logger.info("Matched notification #" + cfg.getId() + " for process state change #" + pi.getInternalId());
                List<String> emailsToNotify = new LinkedList<String>();
                if (task != null && cfg.isNotifyTaskAssignee()) {
                	//TODO: Zmienić na pobieranie ownera ZADANIA, a nie osoby PROCESU (czyli osoby rozliczanej)!!
                    UserData owner = task.getOwner();
                    if (cfg.isSkipNotificationWhenTriggeredByAssignee() &&
                            owner != null &&
                            owner.getLogin() != null &&
                            owner.getLogin().equals(userData.getLogin())) {
                        logger.info("Not notifying user " + owner.getLogin() + " - this user has initiated processed action");
                        continue;
                    }
                    if (owner != null && hasText(owner.getEmail())) {
                        emailsToNotify.add(owner.getEmail());
                        logger.info("Notification will be sent to " + owner.getEmail());
                    }
                }
                if (hasText(cfg.getNotifyEmailAddresses())) {
                    emailsToNotify.addAll(Arrays.asList(cfg.getNotifyEmailAddresses().split(",")));
                }
				if (hasText(cfg.getNotifyUserAttributes())) {
					emailsToNotify.addAll(extractUserEmails(cfg.getNotifyUserAttributes()));
				}
                if (emailsToNotify.isEmpty()) {
                    logger.info("Despite matched rules, no emails qualify to notify for cfg #" + cfg.getId());
                    continue;
                }
                String templateName = cfg.getTemplateName();
                
                BpmNotificationTemplate template = templateProvider.getBpmNotificationTemplate(templateName);

                ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
                Map data = prepareData(task, pi, userData, cfg, ctx);
                String body = processTemplate(templateName, data);
                String subject = processTemplate(templateName + SUBJECT_TEMPLATE_SUFFIX, data);

                javax.mail.Session mailSession = mailSessionProvider.getSession(cfg.getProfileName());

                for (String rcpt : new HashSet<String>(emailsToNotify)) {
                        sendEmail(rcpt, template.getSender(), subject, body, cfg.isSendHtml(), mailSession);
                }
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }
    
    /** Register mail session provider. There is support for:
     * <li> Database configuration (mail.settings.provider.type = database)
     * <li> JNDI resource configuration (mail.settings.provider.type = jndi)
     * 
     * If configuration in pt_settings is not set, default is database
     */
    public void registerMailSettingProvider()
    {	
    	/* Look for configuration for mail provider. If none exists, default is database */
    	String providerName = ProcessToolContext.Util.getThreadProcessToolContext().getSetting(PROVIDER_TYPE);
    	
    	if(providerName == null)
    	{
    		logger.warning("Mail session provider type is not set, using default database provider");
    		mailSessionProvider = new DatabaseMailSessionProvider();
    	}
    	else if(providerName.equals("database"))
    	{
    		logger.info("Mail session provider set to database");
    		mailSessionProvider = new DatabaseMailSessionProvider();
    	}
    	else if(providerName.equals("jndi"))
    	{
    		logger.info("Mail session provider set to jndi resources");
    		mailSessionProvider = new JndiMailSessionProvider();
    	}
    	else
    	{
    		logger.severe("Unknown provider ["+providerName+"]! Service will be stopped");
    		//throw new IllegalArgumentException("Unknown provider ["+providerName+"]! Service will be stopped");
    	}
    	
    	
    }

	private Collection<String> extractUserEmails(String notifyUserAttributes) {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		Set<String> emails = new HashSet<String>();
		for (String attribute : notifyUserAttributes.split(",")) {
			attribute = attribute.trim();
			if (hasText(attribute)) {
				UserData user = ctx.getUserDataDAO().loadUserByLogin(attribute);
				emails.add(user.getEmail());
			}
		}
		return emails;
	}

    public void sendNotification(String recipient, String subject, String body) throws Exception {
        sendNotification(null, recipient, subject, body);
    }

    public void sendNotification(String mailSessionProfileName, String recipient, String subject, String body) throws Exception {
        sendNotification(mailSessionProfileName, null, recipient, subject, body);
    }

    public void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body) throws Exception {
        refreshConfigIfNecessary();
        javax.mail.Session mailSession = mailSessionProvider.getSession(mailSessionProfileName);
        if (!Strings.hasText(sender)) {
            UserData autoUser = ProcessToolContext.Util.getThreadProcessToolContext().getAutoUser();
            sender = autoUser.getEmail();
        }
        sendEmail(recipient, sender, subject, body, true, mailSession);
    }

    private Map prepareData(BpmTask task, ProcessInstance pi, UserData userData, BpmNotificationConfig cfg, ProcessToolContext ctx) {
        Map m = new HashMap();
        if (task != null) {
            m.put("task", task);

            Locale locale = Strings.hasText(cfg.getLocale()) ? new Locale(cfg.getLocale()) : Locale.getDefault();
            messageSource.setLocale(locale);
            for (ProcessStateConfiguration st : pi.getDefinition().getStates()) {
                if (task.getTaskName().equals(st.getName())) {
                    m.put("taskName", messageSource.getMessage(st.getDescription()));
                    break;
                }
            }

            m.put("taskUrl", getTaskLink(task, ctx));
        }
        m.put("processVisibleId", Strings.hasText(pi.getExternalKey()) ? pi.getExternalKey() : pi.getInternalId());
        m.put("process", pi);
        m.put("user", userData);
        m.put("session", bpmSession);
        m.put("context", ctx);
        m.put("config", cfg);

		if (hasText(cfg.getTemplateArgumentProvider())) {
			for (TemplateArgumentProvider argumentProvider : argumentProviders) {
				if (cfg.getTemplateArgumentProvider().equalsIgnoreCase(argumentProvider.getName())) {
					TemplateArgumentProviderParams params = new TemplateArgumentProviderParams();
					params.setProcessInstance(pi);
					argumentProvider.getArguments(m, params);
					break;
				}
			}
		}

        return m;
    }


	@Override
	public void registerTemplateArgumentProvider(TemplateArgumentProvider provider) {
		argumentProviders.add(provider);
	}

	@Override
	public void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider) {
		argumentProviders.add(provider);
	}

	private void sendEmail(String rcpt, String from, String subject, String body, boolean sendHtml, javax.mail.Session mailSession) throws Exception {
        if (!Strings.hasText(rcpt)) {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        logger.info("Sending mail to " + rcpt + " from " + from);
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(rcpt));
        message.setSubject(subject);
        message.setContent(body, (sendHtml ? "text/html" : "text/plain") + "; charset=utf-8");
        message.setSentDate(new Date());
        
    	logger.info("About to send message: " + 
    			"\nSubject: " + message.getSubject() + 
    			"\nContent: " + message.getContent() +
    			"\n"
    			);
        
        sendMessage(message, mailSession);
    }

    public synchronized void refreshConfigIfNecessary() {
        if (cacheUpdateTime + CONFIG_CACHE_REFRESH_INTERVAL < System.currentTimeMillis()) {
            Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
            configCache = session
                    .createCriteria(BpmNotificationConfig.class)
                    .add(Restrictions.eq("active", true))
                    .list();

            cacheUpdateTime = System.currentTimeMillis();
            
            registerMailSettingProvider();
            
            /* Refresh config for providers */
            templateProvider.refreshConfig();
            mailSessionProvider.refreshConfig();

            persistentMailProperties = new HashMap<String, Properties>();
            List<BpmNotificationMailProperties> properties = session.createCriteria(BpmNotificationMailProperties.class).list();
            for (BpmNotificationMailProperties bnmp : properties) 
            {
                if (hasText(bnmp.getProfileName())) 
                {
                    Properties prop = new Properties();
                    
                    if(bnmp.isDebug())
                    {
                    	logger.info(" mail.smtp.host = "+bnmp.getSmtpHost() +
                    		"\n mail.smtp.socketFactory.port = "+bnmp.getSmtpSocketFactoryPort() +
                    		"\n mail.smtp.socketFactory.class = "+bnmp.getSmtpSocketFactoryClass() +
                    		"\n mail.smtp.auth = "+bnmp.isSmtpAuth() +
                    		"\n mail.smtp.port = "+bnmp.getSmtpPort() +
                    		"\n mail.smtp.user = "+bnmp.getSmtpUser() +
                    		"\n mail.debug = "+bnmp.isDebug() +
                    		"\n mail.smtp.starttls.enable = "+bnmp.isStarttls());
                    }
                    
                    if(bnmp.getSmtpHost() != null)
                    	prop.put("mail.smtp.host",  bnmp.getSmtpHost());
                    
                    if(bnmp.getSmtpSocketFactoryPort() != null)
                    	prop.put("mail.smtp.socketFactory.port", bnmp.getSmtpSocketFactoryPort());
                    
                    if(bnmp.getSmtpSocketFactoryClass() != null)
                    	prop.put("mail.smtp.socketFactory.class", bnmp.getSmtpSocketFactoryClass());
                    

                    prop.put("mail.smtp.auth", getStringValueFromBoolean(bnmp.isSmtpAuth()));
                    
                    if(bnmp.getSmtpPort() != null)
                    	prop.put("mail.smtp.port", bnmp.getSmtpPort());
                    
                    if(bnmp.getSmtpUser() != null)
                    	prop.put("mail.smtp.user", bnmp.getSmtpUser());
                    
                    if(bnmp.getSmtpPassword() != null)
                    	prop.put("mail.smtp.password", bnmp.getSmtpPassword());
                    
                    prop.put("mail.debug", getStringValueFromBoolean(bnmp.isDebug()));
                    prop.put("mail.smtp.starttls.enable", getStringValueFromBoolean(bnmp.isStarttls()));
                    
                    persistentMailProperties.put(bnmp.getProfileName(), prop);
                }
                else {
                    logger.log(Level.WARNING, "Unable to determine profile name for mail config with id: " + bnmp.getId());
                }
            }

            bpmSession = ProcessToolContext.Util.getThreadProcessToolContext().getProcessToolSessionFactory().createAutoSession();
        }

    }
    
    private String getStringValueFromBoolean(Boolean value)
    {
    	if(value == null)
    		return "false";
    	
    	if(value)
    		return "true";
    	else
    		return "false";
    }
    
    public void sendNotification(String mailSessionProfileName, String sender, String recipient, String subject, String body, List<String> attachments) throws Exception {

        refreshConfigIfNecessary();
        javax.mail.Session mailSession = mailSessionProvider.getSession(mailSessionProfileName);
        if (!Strings.hasText(sender)) {
            UserData autoUser = ProcessToolContext.Util.getThreadProcessToolContext().getAutoUser();
            sender = autoUser.getEmail();
        }
        
        if (!Strings.hasText(recipient)) {
            throw new IllegalArgumentException("Cannot send email: Recipient is null!");
        }
        Message message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(sender));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        
        //body
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setText(body);
        
    	logger.info("About to send message: " + 
    			"\nSubject: " + message.getSubject() + 
    			"\nContent: " + body +
    			"\nAttachments: " + attachments.size()
    			);
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messagePart);

        //zalaczniki
        int counter = 0;
        URL url;
        for (String u : attachments) {
        	if (!Strings.hasText(u))
        		continue;
        	url = new URL(u);
	        MimeBodyPart attachmentPart = new MimeBodyPart();
	        URLDataSource urlDs = new URLDataSource(url);
	        attachmentPart.setDataHandler(new DataHandler(urlDs));
	        attachmentPart.setFileName("file" + counter++);
	        multipart.addBodyPart(attachmentPart);
	        logger.info("Added attachment " + u);
        }       
        
        message.setContent(multipart);
        message.setSentDate(new Date());
        
        logger.info("Sending mail with attaments to " + recipient + " from " + sender);
        sendMessage(message, mailSession);
    }
    
    private void sendMessage(Message message, javax.mail.Session mailSession) throws Exception 
    {
        try 
        {
	    	/* If smtps is required, force diffrent transport properties */
	    	if(isSmtpsRequired(mailSession))
	    	{
	    		Properties emailPrtoperties = mailSession.getProperties();
	    		
	    		String secureHost = emailPrtoperties.getProperty("mail.smtp.host");
	    		String securePort = emailPrtoperties.getProperty("mail.smtp.port");
	    		String userName = emailPrtoperties.getProperty("mail.smtp.user");
	    		String userPassword = emailPrtoperties.getProperty("mail.smtp.password");
	    		
	            Transport transport = mailSession.getTransport("smtps");
	            transport.connect(secureHost, Integer.parseInt(securePort), userName, userPassword);
	            transport.sendMessage(message, message.getAllRecipients());
	            transport.close();
	    	}
	    	/* Default transport mechanism */
	    	else
	    	{
	    		Transport.send(message);
	    	}
	    	
	    	 logger.info("Emails sent");
        }
        catch (Exception e) 
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    /** Check is smtps is required */
    private boolean isSmtpsRequired(javax.mail.Session mailSession)
    {
    	//TODO to implement
    	return false;
    }

	@Override
	public String findTemplate(String templateName)
	{
		refreshConfigIfNecessary();
		return templateProvider.findTemplate(templateName);
	}

	@Override
	public String processTemplate(String templateName, Map data)
	{
		refreshConfigIfNecessary();
		return templateProvider.processTemplate(templateName,data);
	}
}
