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
	private String period;
	private String user; 
	private String password;
	private String checklifecycle;

	public String getChecklifecycle() {
		return checklifecycle.trim();
	}
	public void setChecklifecycle(String checklifecycle) {
		this.checklifecycle = checklifecycle;
	}
	public String getPeriod() {
		return period.trim();
	}
	public void setPeriod(String period) {
		this.period = period.trim();
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
	public String getUser() {
		return user.trim();
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password.trim();
	}
	public void setPassword(String password) {
		this.password = password;
	} 
	
	
	

}
