package eu.scapeproject.pt.main;

/**
 * 
 * @author mhn
 *
 */
public class Configuration {
	
	private String dir; 
	private String url;
	private String ingest;
	private String lifecycle;
	private Boolean mode;
	
	public Boolean getMode() {
		return mode;
	}
	public void setMode(Boolean mode) {
		this.mode = mode;
	}
	public String getDir() {
		return dir.trim();
	}
	public void setDir(String dir) {
		if (!dir.endsWith("/")) {
			dir = dir.trim() +"/";
		}
		this.dir = dir;
	}
	public String getUrl() {
		return url.trim();
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIngest() {
		return ingest.trim();
	}
	public void setIngest(String ingest) {
		this.ingest = ingest;
	}
	public String getLifecycle() {
		return lifecycle.trim();
	}
	public void setLifecycle(String lifecycle) {
		this.lifecycle = lifecycle;
	} 
	
	
	

}
