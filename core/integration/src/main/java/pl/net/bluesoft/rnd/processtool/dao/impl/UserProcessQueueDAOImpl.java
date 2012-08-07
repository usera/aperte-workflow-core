package pl.net.bluesoft.rnd.processtool.dao.impl;

import java.util.Collection;

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
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<UserProcessQueue> getAllUserProcessQueueElementsByProcessId(String processId)
	{
        return (Collection<UserProcessQueue>)session.createCriteria(UserProcessQueue.class)
                .add(Restrictions.eq("processId", processId))
                .list();
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
