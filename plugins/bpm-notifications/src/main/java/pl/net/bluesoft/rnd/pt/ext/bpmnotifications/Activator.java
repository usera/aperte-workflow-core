package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEvent;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEventListener;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationConfig;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationTemplate;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.util.eventbus.EventListener;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> {

	private BpmNotificationEngine engine;
	private MailEventListener mailEventListener;
	private SchedulersActivator schedulerActivator;
	
	@Override
	public void start(BundleContext context) throws Exception 
	{
		ProcessToolRegistry registry = getRegistry(context);
		
		schedulerActivator = new SchedulersActivator(registry);
		
		/* Init the bpm notification engine */
		engine = new BpmNotificationEngine(registry);
	
		registry.registerModelExtension(BpmNotificationConfig.class, BpmNotificationTemplate.class, BpmNotificationMailProperties.class);
		registry.commitModelExtensions();
        registry.registerService(BpmNotificationService.class, engine, new Properties());
		registry.getEventBusManager().subscribe(BpmEvent.class, this);
		
		mailEventListener = new MailEventListener(engine);
		registry.getEventBusManager().subscribe(MailEvent.class, mailEventListener);
		
		/* Register scheduler for notifications sending */
		schedulerActivator.scheduleNotificationsSend(engine);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ProcessToolRegistry registry = getRegistry(context);
        registry.removeRegisteredService(BpmNotificationService.class);
		registry.getEventBusManager().unsubscribe(BpmEvent.class, this);
		registry.getEventBusManager().unsubscribe(MailEvent.class, mailEventListener);
		mailEventListener = null;
	}

	private ProcessToolRegistry getRegistry(BundleContext context) {
		ServiceReference ref = context.getServiceReference(ProcessToolRegistry.class.getName());
		return (ProcessToolRegistry) context.getService(ref);
	}

	public void onEvent(BpmEvent e) {
        if (Type.ASSIGN_TASK == e.getEventType() || Type.NEW_PROCESS == e.getEventType()) {
            engine.onProcessStateChange(e.getTask(), e.getProcessInstance(),
                    e.getUserData(), BpmEvent.Type.NEW_PROCESS == e.getEventType());
        }
	}
	
}
