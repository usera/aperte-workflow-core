package pl.net.bluesoft.rnd.processtool.userqueues;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

/**
 * Manager for user process queues, which provides logic to manipulate queues according
 * do process changes
 * 
 * @author mpawlak
 *
 */
public interface IUserProcessQueueManager
{
	void updateQueues(ProcessInstance processInstance, BpmTask bpmTask);
}
