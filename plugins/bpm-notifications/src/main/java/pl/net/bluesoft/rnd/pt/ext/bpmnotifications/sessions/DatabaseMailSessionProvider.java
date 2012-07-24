package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;

import static pl.net.bluesoft.util.lang.Strings.hasText;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.PasswordAuthentication;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;

/**
 * Mail session provider based on configuration from database
 * @author mpawlak
 *
 */
public class DatabaseMailSessionProvider implements IMailSessionProvider
{
    
	private Logger logger = Logger.getLogger(DatabaseMailSessionProvider.class.getName());
	
	private Map<String, Properties> persistentMailProperties = new HashMap<String, Properties>();
	private Properties mailProperties;
    
	@Override
	public javax.mail.Session getSession(String profileName)
	{
		Boolean profileExists = hasText(profileName) && persistentMailProperties.containsKey(profileName);
		Properties properties = null;
		
		if(profileExists)
		{
			properties = persistentMailProperties.get(profileName);
		}
		else
		{
			properties = mailProperties;
			logger.warning("Warning, profile "+profileName+" doesn't exist in configuration, using default from mail.properties!");
		}
		
		/* Get user name and password from configuration */
		String userName = properties.getProperty("mail.smtp.user");
		String userPassword = properties.getProperty("mail.smtp.password");
		String isDebug = properties.getProperty("mail.debug");
		
		final PasswordAuthentication authentication = new PasswordAuthentication(userName, userPassword);
		
		javax.mail.Session mailSession = javax.mail.Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return authentication;
                    }
                });
		
		/* If set, enable debug informations */
		if(isDebug != null && isDebug.equals("true"))
			mailSession.setDebug(true);
		
		return mailSession;
	}
	
    public synchronized void refreshConfig() 
    {
        Session session = ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();

        Properties p = new Properties();
        try {
            p.load(getClass().getResourceAsStream("/pl/net/bluesoft/rnd/pt/ext/bpmnotifications/mail.properties"));
            mailProperties = p;
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }

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

}
