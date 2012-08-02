package org.aperteworkflow.ui.view;

import java.io.Serializable;

public class ViewEvent implements Serializable {
	
	public enum Type {
		ACTION_COMPLETE
	}
	
	private Type type;

	public ViewEvent(Type type) {
		super();
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}	
}
