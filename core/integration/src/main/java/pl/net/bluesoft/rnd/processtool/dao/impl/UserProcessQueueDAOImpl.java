package pl.net.bluesoft.rnd.processtool.dao.impl;

import org.hibernate.Session;

import pl.net.bluesoft.rnd.processtool.dao.UserProcessQueueDAO;
import pl.net.bluesoft.rnd.processtool.hibernate.SimpleHibernateBean;
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
	public long saveUserProcessQueue(UserProcessQueue userProcessQueue) 
	{
		// TODO Auto-generated method stub
		return 0;
	}


}
