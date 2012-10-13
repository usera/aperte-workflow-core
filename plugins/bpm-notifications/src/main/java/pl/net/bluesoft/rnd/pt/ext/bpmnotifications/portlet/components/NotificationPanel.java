package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationConfigDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.List;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:23
 */
public class NotificationPanel extends ItemEditorLayout<BpmNotificationConfig> {
	private TextField profileName;
	private TextField templateName;
	private TextField templateArgumentProvider;
	private CheckBox active;
	private CheckBox sendHtml;
	private TextField localeField;
	private TextField processTypeRegex;
	private TextField stateRegex;
	private TextField lastActionRegex;
	private CheckBox notifyOnProcessStart;
	private CheckBox notifyOnProcessEnd;
	private CheckBox onEnteringStep;
	private CheckBox skipNotificationWhenTriggeredByAssignee;
	private CheckBox notifyTaskAssignee;
	private TextField notifyEmailAddresses;
	private TextField notifyUserAttributes;

	public NotificationPanel(I18NSource i18NSource) {
		super(BpmNotificationConfig.class, i18NSource);
		buildLayout();
	}

	@Override
	protected Component createItemDetailsLayout() {
		FormLayout formLayout = new FormLayout();

		formLayout.addComponent(profileName = textField("Profil", 400));
		formLayout.addComponent(templateName = textField("Szablon", 400));
		formLayout.addComponent(templateArgumentProvider = textField("Dostawca parametrów", 400));
		formLayout.addComponent(active = checkBox("Aktywny"));
		formLayout.addComponent(sendHtml = checkBox("Wyślij jako HTML"));
		formLayout.addComponent(localeField = textField("Locale"));

		formLayout.addComponent(processTypeRegex = textField("Proces", 400));
		formLayout.addComponent(stateRegex = textField("Stan", 400));
		formLayout.addComponent(lastActionRegex = textField("Ostatnia akcja", 400));

		formLayout.addComponent(notifyOnProcessStart = checkBox("Wyślij przy utworzeniu procesu"));
		formLayout.addComponent(notifyOnProcessEnd = checkBox("Wyślij przy zakończeniu procesu"));
		formLayout.addComponent(onEnteringStep = checkBox("Wyślij przy wejściu w krok (???)"));
		formLayout.addComponent(skipNotificationWhenTriggeredByAssignee = checkBox("Pomiń, gdy wyzwolone przez osobę przypisaną"));

		formLayout.addComponent(notifyTaskAssignee = checkBox("Powiadom osobę przypisaną"));
		formLayout.addComponent(notifyEmailAddresses = textField("Wyślij na adresy", 400));
		formLayout.addComponent(notifyUserAttributes = textField("Wyślij do użytkowników", 400));
		formLayout.addComponent(new Label(
				getMessage("Loginy użytkowników/atrybuty procesu ujęte w #{ } należy rozdzielić przecinkami.<br/>Np. <i>pracownik,#{owner}</i>"), Label.CONTENT_XHTML));

		return formLayout;
	}

	@Override
	protected void clearDetails() {
		profileName.setValue(null);
		templateName.setValue(null);
		templateArgumentProvider.setValue(null);
		active.setValue(null);
		sendHtml.setValue(null);
		localeField.setValue(null);
		processTypeRegex.setValue(null);
		stateRegex.setValue(null);
		lastActionRegex.setValue(null);
		notifyOnProcessStart.setValue(null);
		notifyOnProcessEnd.setValue(null);
		onEnteringStep.setValue(null);
		skipNotificationWhenTriggeredByAssignee.setValue(null);
		notifyTaskAssignee.setValue(null);
		notifyEmailAddresses.setValue(null);
		notifyUserAttributes.setValue(null);
	}

	@Override
	protected void loadDetails(BpmNotificationConfig item) {
		profileName.setValue(item.getProfileName());
		templateName.setValue(item.getTemplateName());
		templateArgumentProvider.setValue(item.getTemplateArgumentProvider());
		active.setValue(item.isActive());
		sendHtml.setValue(item.isSendHtml());
		localeField.setValue(item.getLocale());
		processTypeRegex.setValue(item.getProcessTypeRegex());
		stateRegex.setValue(item.getStateRegex());
		lastActionRegex.setValue(item.getLastActionRegex());
		notifyOnProcessStart.setValue(item.isNotifyOnProcessStart());
		notifyOnProcessEnd.setValue(item.isNotifyOnProcessEnd());
		onEnteringStep.setValue(item.isOnEnteringStep());
		skipNotificationWhenTriggeredByAssignee.setValue(item.isSkipNotificationWhenTriggeredByAssignee());
		notifyTaskAssignee.setValue(item.isNotifyTaskAssignee());
		notifyEmailAddresses.setValue(item.getNotifyEmailAddresses());
		notifyUserAttributes.setValue(item.getNotifyUserAttributes());
	}

	@Override
	protected void saveDetails(BpmNotificationConfig item) {
		item.setProfileName(getString(profileName));
		item.setTemplateName(getString(templateName));
		item.setTemplateArgumentProvider(getString(templateArgumentProvider));
		item.setActive(getBoolean(active));
		item.setSendHtml(getBoolean(sendHtml));
		item.setLocale(getString(localeField));
		item.setProcessTypeRegex(getString(processTypeRegex));
		item.setStateRegex(getString(stateRegex));
		item.setLastActionRegex(getString(lastActionRegex));
		item.setNotifyOnProcessStart(getBoolean(notifyOnProcessStart));
		item.setNotifyOnProcessEnd(getBoolean(notifyOnProcessEnd));
		item.setOnEnteringStep(getBoolean(onEnteringStep));
		item.setSkipNotificationWhenTriggeredByAssignee(getBoolean(skipNotificationWhenTriggeredByAssignee));
		item.setNotifyTaskAssignee(getBoolean(notifyTaskAssignee));
		item.setNotifyEmailAddresses(getString(notifyEmailAddresses));
		item.setNotifyUserAttributes(getString(notifyUserAttributes));
	}

	@Override
	protected void prepareData() {
		// TODO zaladowanie definicji procesow + szablonow maili + profili
	}

	@Override
	protected List<BpmNotificationConfig> getAllItems() {
		return new BpmNotificationConfigDAO().findAll();
	}

	@Override
	protected String getItemCaption(BpmNotificationConfig item) {
		return item.getTemplateName() + " (" + item.getId() + ")";
	}

	@Override
	protected BpmNotificationConfig createItem() {
		return new BpmNotificationConfig();
	}

	@Override
	protected BpmNotificationConfig refreshItem(Long id) {
		return new BpmNotificationConfigDAO().loadById(id);
	}

	@Override
	protected void saveItem(BpmNotificationConfig item) {
		new BpmNotificationConfigDAO().saveOrUpdate(item);
	}
}
