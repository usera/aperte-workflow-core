package pl.net.bluesoft.rnd.processtool.dao;

import pl.net.bluesoft.rnd.processtool.hibernate.HibernateBean;
import pl.net.bluesoft.rnd.processtool.hibernate.ResultsPageWrapper;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserProcessQueue;

import java.util.*;

/**
 * DAO for user process queue operations
 * 
 * @author Maciej Pawlak
 *
 */
public interface UserProcessQueueDAO extends HibernateBean<UserProcessQueue> 
{
	
	/** Get all users process queue elements by given process id */
	Collection<UserProcessQueue> getAllUserProcessQueueElementsByProcessId(String processId);
	
    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OTHERS_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToOthers(String processId, String creatorLogin);

    /** Methods returns the user process queue element with given id, created by user with given login and
     * with type = OWN_ASSIGNED
     */
	UserProcessQueue getUserProcessAssignedToHim(String processId, String creatorLogin);
}
