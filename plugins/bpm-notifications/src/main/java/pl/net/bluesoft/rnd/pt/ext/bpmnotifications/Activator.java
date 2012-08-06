package pl.net.bluesoft.rnd.pt.ext.bpmnotifications;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent;
import pl.net.bluesoft.rnd.processtool.bpm.BpmEvent.Type;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEvent;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.event.MailEventListener;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service.BpmNotificationService;
import pl.net.bluesoft.util.eventbus.EventListener;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class Activator implements BundleActivator, EventListener<BpmEvent> {
	
    private Logger logger = Logger.getLogger(Activator.class.getName());

	BpmNotificationEngine engine = new BpmNotificationEngine();
	MailEventListener mailEventListener;
	
	@Override
	public void start(BundleContext context) throws Exception {
		ProcessToolRegistry registry = getRegistry(context);
//		registry.registerModelExtension(BpmNotificationConfig.class, BpmNotificationTemplate.class, BpmNotificationMailProperties.class);
//		registry.commitModelExtensions();
        registry.registerService(BpmNotificationService.class, engine, new Properties());
		registry.getEventBusManager().subscribe(BpmEvent.class, this);
		
		mailEventListener = new MailEventListener(engine);
		registry.getEventBusManager().subscribe(MailEvent.class, mailEventListener);
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

	public void onEvent(BpmEvent event) {
		String eventString = event.toString();
		try {
			logger.log(Level.INFO, "Received " + eventString);
	        if (Type.ASSIGN_TASK == event.getEventType() || Type.NEW_PROCESS == event.getEventType() || Type.SIGNAL_PROCESS == event.getEventType()) {
	            boolean processStarted = BpmEvent.Type.NEW_PROCESS == event.getEventType();
	            boolean enteringStep = Type.ASSIGN_TASK == event.getEventType() || Type.NEW_PROCESS == event.getEventType();
				engine.onProcessStateChange(event.getTask(), event.getProcessInstance(),
	                    event.getUserData(), processStarted, enteringStep);
	        }
		} catch(Throwable t) {
			logger.log(Level.INFO, "Exception in BpmNotificationEngine on event " + eventString);
		}
	}
}
