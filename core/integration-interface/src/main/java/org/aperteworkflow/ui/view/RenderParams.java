package org.aperteworkflow.ui.view;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

/**
 * User: POlszewski
 * Date: 2012-07-19
 * Time: 10:49
 */
public abstract class RenderParams {
	private I18NSource i18NSource;
	private ProcessToolBpmSession bpmSession;
	private ProcessToolContext context;

	public I18NSource getI18NSource() {
		return i18NSource;
	}

	public void setI18NSource(I18NSource i18NSource) {
		this.i18NSource = i18NSource;
	}

	public ProcessToolBpmSession getBpmSession() {
		return bpmSession;
	}

	public void setBpmSession(ProcessToolBpmSession bpmSession) {
		this.bpmSession = bpmSession;
	}

	public ProcessToolContext getContext() {
		return context;
	}

	public void setContext(ProcessToolContext context) {
		this.context = context;
	}

	public interface TransactionCallback {
		void invoke(ProcessToolContext ctx, ProcessToolBpmSession session);
	}

	public abstract void withTransaction(TransactionCallback callback);
}
