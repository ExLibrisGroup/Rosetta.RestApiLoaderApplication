package eu.scapeproject;

import java.net.URI;


public class Sip {

	public enum STATE {PENDING, IN_PROGRESS, SUBMITTED_TO_REPOSITORY, FAILED, INGESTING, INGESTED};

	private long id;
	private String sipId;
	private URI uri;
	private STATE state;
	private String description;

	public Sip() {
	}

	public Sip(long id, String sipId, URI uri, STATE state, String description) {
		this.id = id;
		this.sipId = sipId;
		this.uri = uri;
		this.state = state;
		this.description = description;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSipId() {
		return sipId;
	}

	public void setSipId(String sipId) {
		this.sipId = sipId;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public STATE getState() {
		return state;
	}

	public void setState(STATE state) {
		this.state = state;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Sip [id=" + id + ", sipId=" + sipId + ", uri=" + uri
				+ ", state=" + state + ", description=" + description + "]";
	}
	
	
}
