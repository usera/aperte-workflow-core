package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;

/**
 * Manager for the process instance queues
 * 
 * @author Maciej Pawlak
 *
 */
public class ProcessToolQueueManager 
{

	public static void updateQueues(BpmTask userTask)
	{
		String processExternalKey = userTask.getProcessInstance().getExternalKey();
	}

}
