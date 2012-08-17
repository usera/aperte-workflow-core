package pl.net.bluesoft.rnd.processtool.filters.factory;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * Filter factory to encapsulte filter creation logic
 * @author Maciej Pawlak
 *
 */
public class ProcessInstanceFilterFactory 
{
	private I18NSource source;

	public ProcessInstanceFilterFactory(I18NSource source) 
	{
		this.source = source;
	}
	
	public ProcessInstanceFilterFactory() 
	{
	}

	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createMyTaskDoneByOthersFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,null,getMessage("activity.created.tasks"), QueueType.OWN_IN_PROGRESS, QueueType.OWN_IN_QUEUE);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createOthersTaskAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user,null,user,getMessage("activity.assigned.tasks"), QueueType.OTHERS_ASSIGNED);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createMyTasksAssignedToMeFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,user,getMessage("activity.created.assigned.tasks"), QueueType.OWN_ASSIGNED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createMyClosedTasksFilter(UserData user)
	{
		return getProcessInstanceFilter(user,user,null,getMessage("activity.created.closed.tasks"), QueueType.OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createQueuedTaskFilter(UserData user, ProcessQueue processQueue)
	{
		return getProcessInstanceFilterForQueue(user, processQueue);
	}

	
	/** Methods creates new filter which returns tasks created by given user, but done by others */
	public ProcessInstanceFilter createSubstitutedTaskDoneByOthersFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,null,getMessage("activity.subst.assigned.tasks"), QueueType.OWN_IN_PROGRESS);
	}
	
	/** Methods creates new filter which returns tasks created by other users, but assigned to given user */
	public ProcessInstanceFilter createSubstitutedOthersTaskAssignedToHimFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,null,substitutedUser,getMessage("activity.subst.created.assigned.tasks"), QueueType.OTHERS_ASSIGNED);
	}
	
	/** Methods creates new filter which returns tasks created by given user and assigned to him */
	public ProcessInstanceFilter createSubstitutedTasksAssignedToMeFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,substitutedUser,getMessage("activity.subst.created.tasks"), QueueType.OWN_ASSIGNED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createSubstitutedClosedTasksFilter(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,substitutedUser,null,getMessage("activity.subst.created.closed.tasks"), QueueType.OWN_FINISHED);
	}
	
	/** Methods creates new filter which returns user closed tasks */
	public ProcessInstanceFilter createOtherUserTaskForSubstitutedUser(UserData substitutedUser)
	{
		return getProcessInstanceFilter(substitutedUser,
				null,
				substitutedUser,
				MessageFormat.format(source.getMessage("activity.other.users.tasks"), substitutedUser.getRealName()),
				QueueType.OTHERS_ASSIGNED);
	}
	
	private String getMessage(String key)
	{
		if(source == null)
			return key;
		
		return source.getMessage(key);
	}
	
	private ProcessInstanceFilter getProcessInstanceFilterForQueue(UserData user, ProcessQueue processQueue)
	{
		ProcessInstanceFilter instanceFilter = getProcessInstanceFilter(user,user,null,getMessage("user.queue.name."+processQueue.getName()), QueueType.OWN_IN_QUEUE);
		
		instanceFilter.getQueues().add(processQueue.getName());
		
		return instanceFilter;
	}
	
	private ProcessInstanceFilter getProcessInstanceFilter(UserData user, UserData creator, UserData owner, String name, 
			QueueType ... types)
	{
		ProcessInstanceFilter pif = new ProcessInstanceFilter();
		pif.setFilterOwner(user);
		pif.setName(name);
		
		Set<QueueType> queueTypes = new HashSet<QueueType>();
		for(QueueType queueType: types)
			queueTypes.add(queueType);
		
		pif.setQueueTypes(queueTypes);
		
		if(creator != null)
			pif.getCreators().add(creator);

		if(owner != null)
			pif.getOwners().add(owner);

		return pif;
	}

}
