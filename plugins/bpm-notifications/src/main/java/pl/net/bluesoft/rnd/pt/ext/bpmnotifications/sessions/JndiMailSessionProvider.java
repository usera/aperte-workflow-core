package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;



import java.util.logging.Logger;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.URLName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Session provider for e-mail basen on jndi
 * 
 * @author mpawlak
 *
 */
public class JndiMailSessionProvider implements IMailSessionProvider
{
	private Logger logger = Logger.getLogger(JndiMailSessionProvider.class.getName());

	@Override
	public Session getSession(String profileName)
	{
		Session mailSession = tryLookupForSession("mail/"+profileName);
		
		if(mailSession == null)
			mailSession = tryLookupForSession("java:comp/env/mail/"+profileName);
		
		if(mailSession == null)
		{
			logger.severe("Connection name for jndi resource not found: "+profileName);
			throw new IllegalArgumentException("Connection name for jndi resource not found: "+profileName);
		}
		
		/* Add smtp authentication */
		
		String userName = mailSession.getProperties().getProperty("mail.smtp.user");
		String userPassword = mailSession.getProperties().getProperty("mail.smtp.password");
		String isDebug = mailSession.getProperties().getProperty("mail.debug");
		
		if(isDebug != null && isDebug.equals("true"))
		{
			for(Object property: mailSession.getProperties().keySet())
			{
				Object value = mailSession.getProperties().get(property);
				
				logger.info("Property "+property+" = "+value);
			}
		}
		
		
		if(userPassword == null)
			userPassword = mailSession.getProperties().getProperty("password");
		
		PasswordAuthentication authentication = new PasswordAuthentication(userName,userPassword);

	    URLName url=  new URLName(
	    		mailSession.getProperties().getProperty("mail.transport.protocol"),
	        mailSession.getProperties().getProperty("mail.smtp.host"),
	        -1, null, userName, null);
	    
	    mailSession.setPasswordAuthentication(url,authentication);
		
		return mailSession;
	}
	
	private Session tryLookupForSession(String profileName)
	{
		try
		{
			return (Session) new InitialContext().lookup(profileName);
		}
		catch(NamingException e)
		{
			logger.fine("Connection name for jndi resource not found: "+profileName);
			return null;
		}
	}

	@Override
	public void refreshConfig()
	{
		// TODO Auto-generated method stub

	}

}
