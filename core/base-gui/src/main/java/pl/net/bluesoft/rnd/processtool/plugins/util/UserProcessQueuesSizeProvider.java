package pl.net.bluesoft.rnd.processtool.plugins.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.filters.factory.ProcessInstanceFilterFactory;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceFilter;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.ProcessQueue;
import pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry;

/**
 * User Process Queues size provider. For given user login it
 * returns map witch queue_id as key and size of those queues
 * as values
 * 
 * @author Maciej Pawlak
 *
 */
public class UserProcessQueuesSizeProvider 
{
	private Map<String, Integer> userProcessQueueSize;
	private String userLogin;
	private UserData userData;
	private ProcessToolBpmSession session;
	private ProcessToolRegistry reg;
	private ProcessToolContext ctx;
	
	public UserProcessQueuesSizeProvider(ProcessToolRegistry reg, String userLogin) 
	{
		this.userProcessQueueSize = new HashMap<String, Integer>();
		this.reg = reg;
		this.userLogin = userLogin;
	}
	
	/** Map with queue id as key and its size as value */
	public Map<String, Integer> getUserProcessQueueSize()
	{
		
		fillUserQueuesMap();
		
		return userProcessQueueSize;
	}
	
	/** Initialize new session and context for database access */
	private void fillUserQueuesMap()
	{
		reg.getProcessToolContextFactory().withProcessToolContext(new ProcessToolContextCallback() 
		{
			@Override
			public void withContext(ProcessToolContext ctx) 
			{
				ProcessToolContext.Util.setThreadProcessToolContext(ctx);
				
				UserProcessQueuesSizeProvider.this.userData = reg.getUserDataDAO(ctx.getHibernateSession()).loadUserByLogin(userLogin);
				
				UserProcessQueuesSizeProvider.this.session = ctx.getProcessToolSessionFactory().createSession(userData, userData.getRoleNames());
				UserProcessQueuesSizeProvider.this.ctx = ctx;
				
				fillUserQueuesMapWithContexts();
			}
			
		});
	}
	
	private void fillUserQueuesMapWithContexts()
	{
		ProcessInstanceFilterFactory filterFactory = new ProcessInstanceFilterFactory();
		Collection<ProcessInstanceFilter> queuesFilters = new ArrayList<ProcessInstanceFilter>();
		
		queuesFilters.add(filterFactory.createMyTasksAssignedToMeFilter(userData));
		queuesFilters.add(filterFactory.createMyTaskDoneByOthersFilter(userData));
		queuesFilters.add(filterFactory.createOthersTaskAssignedToMeFilter(userData));
		queuesFilters.add(filterFactory.createMyClosedTasksFilter(userData));
		
		
		for(ProcessInstanceFilter queueFilter: queuesFilters)
		{
			int filteredQueueSize = session.getFilteredTasksCount(queueFilter, ctx);
			userProcessQueueSize.put(queueFilter.getName(), filteredQueueSize);
		}
		
		List<ProcessQueue> userAvailableQueues = new ArrayList<ProcessQueue>(session.getUserAvailableQueues(ctx));
		for(ProcessQueue processQueue: userAvailableQueues)
		{
			//queuesFilters.add(filterFactory.createQueuedTaskFilter(userData, processQueue));
			Long processCount = processQueue.getProcessCount();
			
			userProcessQueueSize.put(processQueue.getName(), processCount.intValue());
		}
	}

	

}
