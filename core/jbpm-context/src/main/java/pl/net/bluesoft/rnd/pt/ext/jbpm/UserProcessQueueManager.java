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
	

	public void onTaskAssigne(BpmTask bpmTask)
	{
		/* Check if process is reassigned to its creator */
		boolean isProcessAssiegnedToCreator = isProcessAssiegnedToCreator(bpmTask);
		
		if(isProcessAssiegnedToCreator)
			processTaskAssigneToOwnerQueue(bpmTask);
		else
			processTaskAssigneToOthers(bpmTask);
	}
	
	@Override
	public void onTaskFinished(BpmTask bpmTask) 
	{
		String taskId = bpmTask.getInternalTaskId();
		
		/* Check if process is reassigned to its creator */
		boolean isProcessAssiegnedToCreator = isProcessAssiegnedToCreator(bpmTask);
		
		UserProcessQueue userProcesQueue = queueDao.getUserProcessQueueByTaskId(taskId, bpmTask.getCreator());
		
		if(userProcesQueue != null)
			queueDao.delete(userProcesQueue);
		
		/** If taks was assigned to someone else, there is also queue element assigned to him to delete */
		if(!isProcessAssiegnedToCreator)
		{
			UserProcessQueue otherUserProcessQueue = queueDao.getUserProcessQueueByTaskId(taskId, bpmTask.getAssignee());
			
			if(otherUserProcessQueue != null)
				queueDao.delete(userProcesQueue);
		}
		
	}
	
	@Override
	public void onProcessFinished(ProcessInstance processInstance, BpmTask bpmTask) 
	{
		/* Get all queue elements for given process id and delete them */
		String processId = processInstance.getId().toString();
		String creatorLogin = bpmTask.getCreator();
		
		/* Create new queue element that is stored as finished process */
		UserProcessQueue finishedProcess = new UserProcessQueue();
		finishedProcess.setLogin(creatorLogin);
		finishedProcess.setProcessId(processId);
		finishedProcess.setQueueType(QueueType.OWN_FINISHED);
		
		queueDao.saveOrUpdate(finishedProcess);
	}


	@Override
	public void onProcessHalted(ProcessInstance processInstance, BpmTask task) 
	{
		//deleteProcessAllocations(processInstance);
		
	}
	
	/** Method checks if process should be in "mine assigned to others" queue */
	private boolean isProcessAssiegnedToCreator(BpmTask bpmTask)
	{
		String assigneeLogin = bpmTask.getAssignee();
		String creatorLogin = bpmTask.getCreator();
		
		/** Process is assigned to someone, and this person is not the creator */
		return assigneeLogin == null || creatorLogin.equals(assigneeLogin);
	}
	
	private void deleteProcessAllocations(ProcessInstance processInstance)
	{
		/* Get all queue elements for given process id and delete them */
		String processId = processInstance.getId().toString();
		Collection<UserProcessQueue> processQueueElements = queueDao.getAllUserProcessQueueElements(processId);
		
		/* Delete all elements from queue */
		queueDao.delete(processQueueElements);
	}
	
	/** Assign process to its owner queue */
	private void processTaskAssigneToOwnerQueue(BpmTask bpmTask)
	{
		processTaskAssigne(bpmTask.getInternalTaskId(), bpmTask.getProcessInstance().getId().toString(), bpmTask.getAssignee(), QueueType.OWN_ASSIGNED);
	}
	
	/** Assign owner process to someone else. Create queue element to owner "mine assigned to others"
	 * and element to other person queue "others assigned to me" */
	private void processTaskAssigneToOthers(BpmTask bpmTask)
	{
		processTaskAssigne(bpmTask.getInternalTaskId(), bpmTask.getProcessInstance().getId().toString(), bpmTask.getCreator(), QueueType.OWN_IN_PROGRESS);
		processTaskAssigne(bpmTask.getInternalTaskId(), bpmTask.getProcessInstance().getId().toString(), bpmTask.getAssignee(), QueueType.OTHERS_ASSIGNED);
	}
	
	private void processTaskAssigne(String taskId, String processId, String assigneLogin, QueueType type)
	{
		UserProcessQueue userProcessQueue = queueDao.getUserProcessQueueByTaskId(taskId, assigneLogin);
		
		/* The queue element for given process exists with type "mine assiegned to me". Change its type and save */
		if(userProcessQueue != null)
		{
			userProcessQueue.setQueueType(type);
		}
		/* Otherwise, create new process queue with correct type */
		else 
		{
			userProcessQueue = new UserProcessQueue();
			userProcessQueue.setLogin(assigneLogin);
			userProcessQueue.setProcessId(processId);
			userProcessQueue.setTaskId(taskId);
			userProcessQueue.setQueueType(type);
		}
		
		queueDao.saveOrUpdate(userProcessQueue);
	}
}
