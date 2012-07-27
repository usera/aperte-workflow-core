package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.IMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.sessions.JndiMailSessionProvider;
import pl.net.bluesoft.rnd.pt.ext.testabstract.AperteDataSourceTestCase;

public class TSLSendNotificationTests extends AperteDataSourceTestCase 
{
	public void testEngineForTSL()
	{
		doTest(new AperteTestMethod() 
		{
			@Override
			public void test() 
			{
				BpmNotificationEngine engine = new BpmNotificationEngine(registry);
				registry.registerService(BpmNotificationService.class, engine, new Properties());
				
				try 
				{
					//engine.sendNotification("Default", "awf@bluesoft.net.pl", "awf@bluesoft.net.pl", "test", "testujemy");
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
					fail("Error during mail processing: "+e.getMessage());
				}
			}
		});

	}
	
	public void test_1()
	{
		Properties props = new Properties();

		
		props.put("mail.transport.protocol", "smtp"); 
		props.put("mail.debug", "true"); 
		props.put("mail.smtp.starttls.enable", "false");
		props.put("mail.smtp.host", "bluesoft.home.pl");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.user", "axa-mail");
		props.put("mail.smtp.port", "465");
		props.put("mail.smtp.password", "esod2011");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.port", "465");

		try
		{
			Authenticator auth = new Authenticator() 
			{
				public PasswordAuthentication getPasswordAuthentication()
				{
				return new PasswordAuthentication("axa-mail", "esod2011");
				}
			};
			
			Session session = Session.getInstance(props, auth);
			session.setDebug(true);
	
			MimeMessage msg = new MimeMessage(session);
			msg.setText("test");
			msg.setSubject("test");
			msg.setFrom(new InternetAddress("axa-mail@bluesoft.net.pl"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("mpawlak@bluesoft.net.pl"));
			Transport.send(msg);
		}
		catch (Exception mex)
		{
			mex.printStackTrace();
			fail();
		}
	}

}
