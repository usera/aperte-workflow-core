package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
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
	public UserProcessQueue getUserProcessQueueByTaskId(String taskId, String assigneLogin) 
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
	public UserProcessQueue getUserProcessAssignedToOthers(String processId, String creatorLogin)
	{
		return getUserProcessQueueElement(processId, creatorLogin, QueueType.OTHERS_ASSIGNED);
	}
	
	@Override
	public UserProcessQueue getUserProcessAssignedToHim(String processId, String creatorLogin)
	{
		return getUserProcessQueueElement(processId, creatorLogin, QueueType.OWN_ASSIGNED);
	}
	
	@Override
	public UserProcessQueue getUserProcessAssignedFromOthers(String processId,String assigne) 
	{
		return getUserProcessQueueElement(processId, assigne, QueueType.OTHERS_ASSIGNED);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<UserProcessQueue> getAllUserProcessQueueElements(String processId, QueueType... types) 
	{
		Criteria criteria = session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId));
		
		if(types.length > 0)
			criteria.add(Restrictions.in("queueType", types));
		
		return (Collection<UserProcessQueue>)criteria.list();
	}
	
	private UserProcessQueue getUserProcessQueueElement(String processId, String creatorLogin, QueueType type)
	{
        return (UserProcessQueue)session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId))
                .add(Restrictions.eq("login", creatorLogin))
                .add(Restrictions.eq("queueType", type))
                .uniqueResult();
	}






}
