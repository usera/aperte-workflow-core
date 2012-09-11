package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet;

import static com.vaadin.ui.Window.Notification.POSITION_CENTERED;
import static com.vaadin.ui.Window.Notification.TYPE_HUMANIZED_MESSAGE;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.ui.view.RenderParams;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Bpm Notifications admin panel
 * 
 * @author polszewski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class BpmNotificationsAdminPanel extends VerticalLayout implements ClickListener
{
	private I18NSource i18NSource;
	private ProcessToolRegistry registry;
	
	private HorizontalLayout sendTestEmailLayout;
	private TextField senderTextField;
	private TextField recipientTextField;
	
	private Button refreshCachesBtn;
	private Button sendTestEmailButton;

	public BpmNotificationsAdminPanel(RenderParams params) {
		this.i18NSource = params.getI18NSource();
		this.registry = params.getContext().getRegistry();
		
		initComponents();
		buildLayout();
	}

	private void buildLayout() 
	{
		setWidth("100%");
		setSpacing(true);
		
		sendTestEmailLayout.addComponent(senderTextField);
		sendTestEmailLayout.addComponent(recipientTextField);
		sendTestEmailLayout.addComponent(sendTestEmailButton);
		
		addComponent(refreshCachesBtn);
		addComponent(sendTestEmailLayout);
	}
	
	private void initComponents()
	{
		refreshCachesBtn = new Button(i18NSource.getMessage(i18NSource.getMessage("bpmnot.refresh.config.cache")));
		refreshCachesBtn.addListener((ClickListener)this);
		
		sendTestEmailLayout = new HorizontalLayout();
		sendTestEmailLayout.setSpacing(true);
		
		senderTextField = new TextField();
		senderTextField.setWidth(150, UNITS_PIXELS);
		senderTextField.setInputPrompt(i18NSource.getMessage("bpmnot.send.test.mail.sender"));
		
		recipientTextField = new TextField();
		recipientTextField.setInputPrompt(i18NSource.getMessage("bpmnot.send.test.mail.recipient"));
		recipientTextField.setWidth(150, UNITS_PIXELS);
		
		sendTestEmailButton = new Button(i18NSource.getMessage("bpmnot.send.test.mail.send.button"));
		sendTestEmailButton.addListener((ClickListener)this);
	}

	private BpmNotificationService getService() {
		return registry.getRegisteredService(BpmNotificationService.class);
	}

	@Override
	public void buttonClick(ClickEvent event) 
	{
		if(event.getButton().equals(refreshCachesBtn))
		{
			getService().invalidateCache();
		}
		else if(event.getButton().equals(sendTestEmailButton))
		{
			try 
			{
				String sender = (String)senderTextField.getValue();
				String recipient = (String)recipientTextField.getValue();
				
				if(sender == null || sender.isEmpty())
				{
					informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sender.empty"));
					return;
				}
				
				if(recipient == null || recipient.isEmpty())
				{
					informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.recipient.empty"));
					return;
				}
				
				getService().addNotificationToSend("Default", sender, recipient, "Test E-mail", "This is test. Ignore it!", true);
				informationNotification(i18NSource.getMessage("bpmnot.send.test.mail.sent"));
			} 
			catch (Exception e) 
			{
				informationNotification("Problem: "+e.getMessage());
			}
		}
		
	}
	
    public void informationNotification(String message) {
        Notification notification = new Notification("<b>" + message + "</b>", TYPE_HUMANIZED_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setDelayMsec(5);
        this.getWindow().showNotification(notification);
    }
}
