package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

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

	public void updateQueues(ProcessInstance processInstance, BpmTask bpmTask)
	{
		String processId = processInstance.getId().toString();
		String assigneeLogin = bpmTask.getAssignee();
		String creatorLogin = bpmTask.getCreator();
		
		processMineAssignedToOthers(processId, creatorLogin);
	}
	
	private void processMineAssignedToOthers(String processId, String creatorLogin)
	{
		UserProcessQueue mineProcessAssignedToOthers = getMineAssignedToOthers(processId, creatorLogin);
		
		if(mineProcessAssignedToOthers == null)
		{
			mineProcessAssignedToOthers = new UserProcessQueue();
			mineProcessAssignedToOthers.setLogin(creatorLogin);
			mineProcessAssignedToOthers.setProcessId(processId);
			mineProcessAssignedToOthers.setQueueType(QueueType.OTHERS_ASSIGNED);
		}
		
		queueDao.saveOrUpdate(mineProcessAssignedToOthers);
		
		
	}
	
	private UserProcessQueue getMineAssignedToOthers(String processId, String creatorLogin)
	{
        Criteria criteria = queueDao.getSession().createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId))
                .add(Restrictions.eq("login", creatorLogin));
        
        return (UserProcessQueue)criteria.uniqueResult();
	}

}
