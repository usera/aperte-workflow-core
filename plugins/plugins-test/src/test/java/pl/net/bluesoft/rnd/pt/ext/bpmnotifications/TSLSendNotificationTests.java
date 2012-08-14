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
		final String userName = "axa-mail";
		final String password = "Blue105";
		
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", "192.168.2.12");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.user", userName);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", "588");
		props.put("mail.smtp.auth.plain.disable", "true");
		
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("ssl.SocketFactory.provider", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.class", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
		props.put("ssl.SocketFactory.provider", "pl.net.bluesoft.rnd.pt.ext.bpmnotifications.socket.ExchangeSSLSocketFactory");
//		props.put("mail.transport.protocol", "smtp"); 
//		props.put("mail.debug", "true"); 
//		props.put("mail.smtp.starttls.enable", "false");
//		props.put("mail.smtp.host", "bluesoft.home.pl");
//		props.put("mail.smtp.auth", "true");
//		props.put("mail.smtp.user", "axa-mail");
//		props.put("mail.smtp.port", "465");
//		props.put("mail.smtp.password", "esod2011");
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.socketFactory.port", "465");

		try
		{
			Authenticator auth = new Authenticator() 
			{
				public PasswordAuthentication getPasswordAuthentication()
				{
				return new PasswordAuthentication(userName, password);
				}
			};
			
			Session session = Session.getInstance(props, auth);
			session.setDebug(true);
	
			MimeMessage msg  = new MimeMessage(session);
			msg.setText("test");
			msg.setSubject("test");
			msg.setFrom(new InternetAddress("axa-mail@bluesoft.net.pl"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress("mpawlak@bluesoft.net.pl"));
			
			
    		String secureHost = props.getProperty("mail.smtp.host");
    		String securePort = props.getProperty("mail.smtp.port");
			
    		Transport.send(msg);
    		
//            Transport transport = session.getTransport("smtp");
//            transport.connect(secureHost, Integer.parseInt(securePort), userName, userPassword);
//            transport.sendMessage(msg, msg.getAllRecipients());
//            transport.close();

		}
		catch (Exception mex)
		{
			mex.printStackTrace();
			fail();
		}
	}

}
