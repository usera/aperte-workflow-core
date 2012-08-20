package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;


/**
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueueDAOImpl extends SimpleHibernateBean<UserProcessQueue> implements UserProcessQueueDAO 
{
	
	@Override
	public UserProcessQueue getUserProcessQueueByTaskId(Long taskId, String assigneLogin) 
	{
        return (UserProcessQueue)session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("taskId", taskId))
                .add(Restrictions.eq("login", assigneLogin))
                .uniqueResult();
	}

	public UserProcessQueueDAOImpl(Session session)
	{
		super(session);
	}

	@Override
	public UserProcessQueue getUserProcessAssignedToOthers(Long processId, String creatorLogin)
	{
		return getUserProcessQueueElement(processId, creatorLogin, QueueType.OTHERS_ASSIGNED);
	}
	
	@Override
	public UserProcessQueue getUserProcessAssignedToHim(Long processId, String creatorLogin)
	{
		return getUserProcessQueueElement(processId, creatorLogin, QueueType.OWN_ASSIGNED);
	}
	
	@Override
	public UserProcessQueue getUserProcessAssignedFromOthers(Long processId,String assigne) 
	{
		return getUserProcessQueueElement(processId, assigne, QueueType.OTHERS_ASSIGNED);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<UserProcessQueue> getAllUserProcessQueueElements(Long processId, QueueType... types) 
	{
		Criteria criteria = session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId));
		
		if(types.length > 0)
			criteria.add(Restrictions.in("queueType", types));
		
		return (Collection<UserProcessQueue>)criteria.list();
	}
	
	private UserProcessQueue getUserProcessQueueElement(Long processId, String creatorLogin, QueueType type)
	{
        return (UserProcessQueue)session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId))
                .add(Restrictions.eq("login", creatorLogin))
                .add(Restrictions.eq("queueType", type))
                .uniqueResult();
	}

	@Override
	public int getQueueLength(String userLogin, QueueType queueType) 
	{
		Criteria criteria = session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("login", userLogin))
                .add(Restrictions.eq("queueType", queueType))
                .setProjection(Projections.rowCount());
		
		Long taskCount = (Long)criteria.uniqueResult();
		return taskCount.intValue();
	}








}
