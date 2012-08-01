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
    long saveUserProcessQueue(UserProcessQueue userProcessQueue);
}
