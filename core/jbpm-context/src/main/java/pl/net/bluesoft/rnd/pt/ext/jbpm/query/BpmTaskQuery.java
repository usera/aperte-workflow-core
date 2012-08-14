package pl.net.bluesoft.rnd.pt.ext.jbpm.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;
import org.jbpm.pvm.internal.history.model.HistoryTaskInstanceImpl;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.QueueType;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmTask;

/**
 * Class to build main query to get bpm tasks 
 * 
 * @author Maciej Pawlak
 *
 */
public class BpmTaskQuery 
{
	/** Main query to get task with correlated processes from user process queue */
	public static final String GET_BPM_TASKS_QUERY = 
			"select DISTINCT task.*, process.* " +
			"from pt_user_process_queue queue, jbpm4_hist_actinst task, pt_process_instance process " +
			"where queue.task_id = task.htask_ and process.id = queue.process_id ";
	
	/** Additional condition to main query to add filter for user login to who task and process are assigned */
	private static final String USER_LOGIN_CONDITION = " and queue.user_login = :userLogin ";
	
	/** Additional condition to main query to add filter for queue type */
	private static final String QUEUE_TYPE_CONDITION = " and queue.queue_type = :queueType ";
	
	/** String builder to build query */
	private StringBuilder queryBuilder;
	
	private Collection<QueryParameter> queryParameters;
	
	/** Current session */
	private ProcessToolContext ctx;
	
	/** Limit for results rows */
	private int maxResultsLimit;
	
	public BpmTaskQuery(ProcessToolContext ctx)
	{
		this.ctx = ctx;
		
		queryBuilder = new StringBuilder(GET_BPM_TASKS_QUERY);
		queryParameters = new ArrayList<BpmTaskQuery.QueryParameter>();
	}
	
	/** Add restriction for user login to who process and task are assigned */
	public void addUserLoginCondition(String userLogin)
	{
		addCondition(USER_LOGIN_CONDITION);
		addParameter("userLogin", userLogin);
	}
	
	/** Add restriction for user login to who process and task are assigned */
	public void addQueueTypeCondition(QueueType type)
	{
		addCondition(QUEUE_TYPE_CONDITION);
		addParameter("queueType", type.toString());
	}
	
	/** Get bpm tasks from initialized query */
	@SuppressWarnings("unchecked")
	public Collection<BpmTask> getBpmTasks()
	{
		/* Build query */
		SQLQuery query = getQuery();
		
		/* Get query results */
		List<Object[]> queueResults = query.list();
		
		Collection<BpmTask> result = new ArrayList<BpmTask>();
		
   		
		/* Every row is one queue element with jbpm task as first column and process instance as second */
   		for(Object[] resultRow: queueResults)
   		{
   			
   			HistoryTaskInstanceImpl taskInstance = (HistoryTaskInstanceImpl)resultRow[0];
   			ProcessInstance processInstance = (ProcessInstance)resultRow[1];
   			
   			/* Map process and jbpm task to system's bpm task */
   			BpmTask task = collectTaskFromActivity(taskInstance, processInstance);
   			
   			result.add(task);
   		}
   		
   		return result;
	}
	
	protected void addCondition(String conditionString)
	{
		queryBuilder.append(conditionString);
	}
	
	protected void addParameter(String key, Object value)
	{
		queryParameters.add(new QueryParameter(key, value));
	}
	
	/** Build main query and add stored parameters to it */
	private SQLQuery getQuery()
	{
   		SQLQuery query = ctx.getHibernateSession().createSQLQuery(queryBuilder.toString())
   				.addEntity("task", HistoryTaskInstanceImpl.class)
   				.addEntity("process", ProcessInstance.class);
   		
   		/* Add all parameters */
   		for(QueryParameter parameter: queryParameters)
   		{
   			if(parameter.getValue() instanceof Collection<?>)
   				query.setParameterList(parameter.getKey(), (Collection<?>)parameter.getValue());
   			else
   				query.setParameter(parameter.getKey(), parameter.getValue());
   		}
   		
   		/* Add limit for max rows count */
   		if(getMaxResultsLimit() > 0)
   			query.setMaxResults(getMaxResultsLimit());
   		
   		return query;
	}
	
   	private BpmTask collectTaskFromActivity(HistoryTaskInstanceImpl task, ProcessInstance pi) 
   	{
   		MutableBpmTask t = new MutableBpmTask();
   		t.setProcessInstance(pi);
   		t.setAssignee(task.getHistoryTask().getAssignee());
   		UserData ud = ctx.getUserDataDAO().loadUserByLogin(task.getHistoryTask().getAssignee());
   		if (ud == null) {
   			ud = new UserData();
   			ud.setLogin(task.getHistoryTask().getAssignee());
   		}
   		t.setOwner(ud);
   		t.setTaskName(task.getActivityName());
   		t.setInternalTaskId(task.getHistoryTask().getId());
   		t.setExecutionId(task.getExecutionId());
   		t.setCreateDate(task.getStartTime());
   		t.setFinishDate(task.getEndTime());
   		t.setFinished(false);
   		return t;
   	}
	
   	public int getMaxResultsLimit() {
		return maxResultsLimit;
	}

	public void setMaxResultsLimit(int maxResultsLimit) {
		this.maxResultsLimit = maxResultsLimit;
	}

	/** Class which provied key-object parameter for query */
	private class QueryParameter
	{
		private String key;
		private Object value;
		
		public QueryParameter(String key, Object value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		public Object getValue() {
			return value;
		}
	}

}
