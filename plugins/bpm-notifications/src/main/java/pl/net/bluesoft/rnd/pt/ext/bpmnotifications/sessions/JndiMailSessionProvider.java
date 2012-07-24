package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions;



import java.util.logging.Logger;

import javax.mail.Session;
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
