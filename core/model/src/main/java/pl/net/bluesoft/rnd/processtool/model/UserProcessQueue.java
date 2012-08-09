package pl.net.bluesoft.rnd.processtool.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * The entity represents one process instance which is attached 
 * to specified user's queue. The entity is being used during
 * process status change (assignee change, new subprocess 
 * creation) for the optymalization 
 * 
 * @author Maciej Pawlak
 *
 */
@Entity
@Table(name="pt_user_process_queue")
public class UserProcessQueue extends PersistentEntity
{
	/** User login as string */
	@Column(columnDefinition="user_login")
	private String login;

	/** Type of the queue */
	@Column(columnDefinition="queue_type")
	@Enumerated(EnumType.STRING)
	private QueueType queueType;
	
	/** Process instance external key */
	@Column(columnDefinition="process_external_key")
	private String processExternalKey;
	
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public QueueType getQueueType() {
		return queueType;
	}

	public void setQueueType(QueueType queueType) {
		this.queueType = queueType;
	}

	public String getProcessExternalKey() {
		return processExternalKey;
	}

	public void setProcessExternalKey(String processExternalKey) {
		this.processExternalKey = processExternalKey;
	}

}
