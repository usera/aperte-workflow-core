package pl.net.bluesoft.rnd.processtool.ui.utils;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.ui.Window;


/**
 * Util witch provides tools to support javascript queues refresher
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class QueuesPanelRefresherUtil 
{
	public static String getQueueTaskId(String taskName)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "user-queue-name-"+fixedTaskName;
	}

	public static String getQueueProcessQueueId(String queueId)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "user-queue-name-"+fixedQueueId;
	}

	public static String getSubstitutedQueueTaskId(String taskName, String userLogin)
	{
		/* remove whitespaces */
		String fixedTaskName = StringUtils.trimToEmpty(taskName).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-queue-name-"+fixedTaskName;
	}

	public static String getSubstitutedQueueProcessQueueId(String queueId, String userLogin)
	{
		/* remove whitespaces */
		String fixedQueueId= StringUtils.trimToEmpty(queueId).replace(".", "-").replace(" ", "-");
		
		return "substituted-"+userLogin+"-user-queue-name-"+fixedQueueId;
	}
	
	/** Register button with given button id */
	public static void registerUser(Window mainWindow, String userLogin)
	{
		mainWindow.executeJavaScript("setCurrentUser('"+userLogin+"');");
	}

	public static void unregisterUser(Window mainWindow, String login) 
	{
		mainWindow.executeJavaScript("clearCurrentUser();");
	}
}
