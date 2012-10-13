package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

import java.util.Collection;
import java.util.Map;

/**
 * Bpm notification service 
 * 
 * @author Maciej Pawlak
 *
 */
public interface BpmNotificationService 
{
	/** Create and add notification with given parameter to queue. It will be send in the next scheduler run */
	void addNotificationToSend(String profileName, String sender, String recipient, String subject, String body, boolean sendAsHtml,  String ... attachments) 
			throws Exception;

    String findTemplate(String templateName);

    String processTemplate(String templateName, Map<String, Object> data);

	void registerTemplateArgumentProvider(TemplateArgumentProvider provider);
	void unregisterTemplateArgumentProvider(TemplateArgumentProvider provider);
	Collection<TemplateArgumentProvider> getTemplateArgumentProviders();

	void invalidateCache();
}
