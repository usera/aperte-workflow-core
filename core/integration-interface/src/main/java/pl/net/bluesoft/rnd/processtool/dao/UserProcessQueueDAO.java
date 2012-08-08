package pl.net.bluesoft.rnd.processtool.dao;

import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;

/**
 * DAO for user process queue operations
 * 
 * @author Maciej Pawlak
 *
 */
public interface UserProcessQueueDAO extends HibernateBean<UserProcessQueue> 
{
	/** Get all users process queue elements by given process id and given queue types */
	Collection<UserProcessQueue> getAllUserProcessQueueElements(String processId, QueueType ... types);
	
    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OTHERS_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToOthers(String processId, String creatorLogin);

    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OWN_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToHim(String processId, String creatorLogin);

	/** Get the user queue elements contains process allocation from others */
	UserProcessQueue getUserProcessAssignedFromOthers(String processId, String assigne);

	/** Get user queue element by given taks id 
	 * @param assigneLogin */
	UserProcessQueue getUserProcessQueueByTaskId(String taskId, String assigneLogin);
}
