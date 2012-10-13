package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet.components;

import com.vaadin.ui.*;
import org.hibernate.Hibernate;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessDefinitionConfig;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationConfigDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationMailPropertiesDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao.BpmNotificationTemplateDAO;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.TemplateArgumentProvider;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.cquery.func.F;

import java.util.Collection;
import java.util.List;

import static pl.net.bluesoft.rnd.processtool.ProcessToolContext.Util.getThreadProcessToolContext;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-10-12
 * Time: 21:23
 */
public class NotificationPanel extends ItemEditorLayout<BpmNotificationConfig> {
	private Select profileName;
	private Select templateName;
	private Select templateArgumentProvider;
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

	private List<BpmNotificationMailProperties> mailProperties;
	private List<BpmNotificationTemplate> mailTemplates;
	private Collection<ProcessDefinitionConfig> processDefinitions;
	private Collection<TemplateArgumentProvider> templateArgumentProviders;

	public NotificationPanel(I18NSource i18NSource, ProcessToolRegistry registry) {
		super(BpmNotificationConfig.class, i18NSource, registry);
		buildLayout();
	}

	@Override
	protected Component createItemDetailsLayout() {
		FormLayout formLayout = new FormLayout();

		formLayout.addComponent(profileName = select("Profil", 400));
		formLayout.addComponent(templateName = select("Szablon", 400));
		formLayout.addComponent(templateArgumentProvider = select("Dostawca parametrów", 400));
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
		mailProperties = new BpmNotificationMailPropertiesDAO().findAll();

		mailTemplates = new BpmNotificationTemplateDAO().findAll();

		processDefinitions = getThreadProcessToolContext()
				.getProcessDefinitionDAO().getActiveConfigurations();
		for (ProcessDefinitionConfig processDefinition : processDefinitions) {
			Hibernate.initialize(processDefinition.getStates());
		}

		templateArgumentProviders = getService().getTemplateArgumentProviders();

		bindValues(profileName, from(mailProperties).select(new F<BpmNotificationMailProperties, String>() {
			@Override
			public String invoke(BpmNotificationMailProperties x) {
				return x.getProfileName();
			}
		}).ordered().toList());
		bindValues(templateName, from(mailTemplates).select(new F<BpmNotificationTemplate, String>() {
			@Override
			public String invoke(BpmNotificationTemplate x) {
				return x.getTemplateName();
			}
		}));
		bindValues(templateArgumentProvider, from(templateArgumentProviders).select(new F<TemplateArgumentProvider, String>() {
			@Override
			public String invoke(TemplateArgumentProvider x) {
				return x.getName();
			}
		}));
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
		BpmNotificationConfig item = new BpmNotificationConfig();
		item.setActive(true);
		item.setLocale(I18NSource.ThreadUtil.getThreadI18nSource().getLocale().toString());
		if (mailProperties.size() == 1) {
			item.setProfileName(mailProperties.get(0).getProfileName());
		}
		return item;
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
