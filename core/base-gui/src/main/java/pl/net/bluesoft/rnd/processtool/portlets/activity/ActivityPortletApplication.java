package pl.net.bluesoft.rnd.processtool.portlets.activity;

import static pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmConstants.REQUEST_PARAMETER_TASK_ID;

import java.util.Map;

import org.aperteworkflow.util.vaadin.GenericVaadinPortlet2BpmApplication;

import pl.net.bluesoft.rnd.processtool.ui.activity.ActivityMainPane;
import pl.net.bluesoft.rnd.processtool.ui.utils.QueuesPanelRefresherUtil;

import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

/**
 * @author tlipski@bluesoft.net.pl
 * @author mpawlak@bluesoft.net.pl
 */
public class ActivityPortletApplication extends GenericVaadinPortlet2BpmApplication implements CloseListener
{
	ActivityMainPane amp;

	public ActivityPortletApplication() {
		loginRequired = true;
	}

	@Override
	protected void initializePortlet() 
	{

        amp = new ActivityMainPane(ActivityPortletApplication.this, ActivityPortletApplication.this, bpmSession);
        addListener(new RequestParameterListener(REQUEST_PARAMETER_TASK_ID) {
            @Override
            public void handleRequestParameters(Map<String, String[]> parameters) {
                String[] values = parameters.get(REQUEST_PARAMETER_TASK_ID);
                String bpmTaskId = values[0];
                amp.displayTaskById(bpmTaskId);
            }
        });
		getMainWindow().setContent(amp);
		getMainWindow().addListener((CloseListener)this);
		
	}

	@Override
	protected void renderPortlet() 
	{
		QueuesPanelRefresherUtil.registerUser(getMainWindow(), user.getLogin());
		QueuesPanelRefresherUtil.changeRefresherInterval(getMainWindow());
	}

	@Override
	public void windowClose(CloseEvent e) 
	{
		QueuesPanelRefresherUtil.unregisterUser(getMainWindow(), user.getLogin());
	}
	
	
}
