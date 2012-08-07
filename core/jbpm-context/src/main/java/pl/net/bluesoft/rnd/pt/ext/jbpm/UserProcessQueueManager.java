package pl.net.bluesoft.rnd.pt.ext.jbpm;

import java.util.Collection;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;
import pl.net.bluesoft.rnd.processtool.userqueues.IUserProcessQueueManager;

/**
 * Manager for the process instance queues
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueueManager implements IUserProcessQueueManager
{
	Session session;
	UserProcessQueueDAO queueDao;
	
	public UserProcessQueueManager(Session session, UserProcessQueueDAO userProcessQueueDAO)
	{
		this.session = session;
		this.queueDao = userProcessQueueDAO;
	}
	

	public void onProcessAssigne(ProcessInstance processInstance, BpmTask bpmTask)
	{
		/* Check if process is reassigned to its creator */
		boolean isProcessAssiegnedToCreator = isProcessAssiegnedToCreator(processInstance, bpmTask);
		
		String processId = processInstance.getId().toString();
		//String creatorLogin = bpmTask.getCreator();
		//Collection<UserProcessQueue> processQueueElements = queueDao.getAllUserProcessQueueElementsByProcessId(processId);
		
		
		if(isProcessAssiegnedToCreator)
		{
			processMineAssignedToMe(processInstance, bpmTask);
		}
		else
		{
			/* Create new one with type "mine assigned to others" or if the "assigned to me" exists, reaarange it to new queue */
			processMineAssignedToOthers(processInstance, bpmTask);
		}
	}
	
	/** Method checks if process should be in "mine assigned to others" queue */
	private boolean isProcessAssiegnedToCreator(ProcessInstance processInstance, BpmTask bpmTask)
	{
		String processId = processInstance.getId().toString();
		String assigneeLogin = bpmTask.getAssignee();
		String creatorLogin = bpmTask.getCreator();
		
		/** Process is assigned to someone, and this person is not the creator */
		return assigneeLogin == null || creatorLogin.equals(assigneeLogin);
	}
	
//	
//	/** Delete process from mine assigned to me queue, if it exists */
//	private void deleteMineAssignedToMe(Collection<UserProcessQueue> processQueueElements, String creatorLogin)
//	{
//		for(UserProcessQueue processQueueElement: processQueueElements)
//		{
//			/* Process is assigned to its creator and its type is "mine assigned to me" */
//			if(processQueueElement.getLogin().equals(creatorLogin) && processQueueElement.getQueueType().equals(QueueType.OWN_ASSIGNED))
//				queueDao.delete(processQueueElement);
//		}
//	}
//	
//	/** Delete process from mine assigned to others queue, if it exists */
//	private void deleteMineAssignedToMe(Collection<UserProcessQueue> processQueueElements, String creatorLogin)
//	{
//		for(UserProcessQueue processQueueElement: processQueueElements)
//		{
//			/* Process is assigned to its creator and its type is "mine assigned to me" */
//			if(processQueueElement.getLogin().equals(creatorLogin) && processQueueElement.getQueueType().equals(QueueType.OWN_ASSIGNED))
//				queueDao.delete(processQueueElement);
//		}
//	}
	
	private void processMineAssignedToOthers(ProcessInstance processInstance, BpmTask bpmTask)
	{
		String processId = processInstance.getId().toString();
		String creatorLogin = bpmTask.getCreator();
		
		UserProcessQueue mineProcessAssignedToMe = queueDao.getUserProcessAssignedToHim(processId,creatorLogin);
		UserProcessQueue mineProcessAssignedToOthers = queueDao.getUserProcessAssignedToOthers(processId,creatorLogin);
		
		/* The queue element for given process exists with type "mine assiegned to me". Change its type and save */
		if(mineProcessAssignedToMe != null)
		{
			mineProcessAssignedToMe.setQueueType(QueueType.OTHERS_ASSIGNED);
			queueDao.saveOrUpdate(mineProcessAssignedToMe);
		}
		/* Otherwise, create new process queue with correct type */
		else if(mineProcessAssignedToOthers == null)
		{
			mineProcessAssignedToOthers = new UserProcessQueue();
			mineProcessAssignedToOthers.setLogin(creatorLogin);
			mineProcessAssignedToOthers.setProcessId(processId);
			mineProcessAssignedToOthers.setQueueType(QueueType.OTHERS_ASSIGNED);
			
			queueDao.saveOrUpdate(mineProcessAssignedToOthers);
		}
	}
	
	private void processMineAssignedToMe(ProcessInstance processInstance, BpmTask bpmTask)
	{
		String processId = processInstance.getId().toString();
		String creatorLogin = bpmTask.getCreator();
		
		UserProcessQueue mineProcessAssignedToOthers = queueDao.getUserProcessAssignedToOthers(processId,creatorLogin);
		UserProcessQueue mineProcessAssignedToMe = queueDao.getUserProcessAssignedToHim(processId,creatorLogin);
		
		/* The queue element for given process exists with type "mine assiegned to others". Change its type and save */
		if(mineProcessAssignedToOthers != null)
		{
			mineProcessAssignedToOthers.setQueueType(QueueType.OWN_ASSIGNED);
			queueDao.saveOrUpdate(mineProcessAssignedToOthers);
		}
		/* Otherwise, create new process queue with correct type */
		else if(mineProcessAssignedToMe == null)
		{
			mineProcessAssignedToMe = new UserProcessQueue();
			mineProcessAssignedToMe.setLogin(creatorLogin);
			mineProcessAssignedToMe.setProcessId(processId);
			mineProcessAssignedToMe.setQueueType(QueueType.OWN_ASSIGNED);
			
			queueDao.saveOrUpdate(mineProcessAssignedToMe);
		}
	}


}
