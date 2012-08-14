package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.portlet;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import org.aperteworkflow.ui.view.RenderParams;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-07-31
 * Time: 13:46
 */
public class BpmNotificationsAdminPanel extends VerticalLayout {
	private I18NSource i18NSource;
	private ProcessToolRegistry registry;

	public BpmNotificationsAdminPanel(RenderParams params) {
		this.i18NSource = params.getI18NSource();
		this.registry = params.getContext().getRegistry();
		buildLayout();
	}

	private void buildLayout() {
		setWidth("100%");
		Button refreshCachesBtn = createRefreshConfigCacheButton();
		addComponent(refreshCachesBtn);
	}

	private Button createRefreshConfigCacheButton() {
		Button refreshCachesBtn = new Button(i18NSource.getMessage(i18NSource.getMessage("bpmnot.refresh.config.cache")));
		refreshCachesBtn.addListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent clickEvent) {
				getService().invalidateCache();
			}
		});
		return refreshCachesBtn;
	}

	private BpmNotificationService getService() {
		return registry.getRegisteredService(BpmNotificationService.class);
	}
}
