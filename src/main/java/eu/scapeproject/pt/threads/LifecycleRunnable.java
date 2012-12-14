package eu.scapeproject.pt.threads;

import java.net.URLEncoder;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.LoaderDao;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;
import eu.scapeproject.pt.auth.EsciDocAuthentication;
import eu.scapeproject.pt.auth.IAuthentication;


public class LifecycleRunnable implements Runnable {

	private ScheduledExecutorService executor; 
	private LoaderApplication loaderapp; 
	private LoaderDao loaderDao;
	static final boolean DONT_INTERRUPT_IF_RUNNING = true; 

	
	private static Logger logger =  Logger.getLogger(LifecycleRunnable.class.getName());
	
	public LifecycleRunnable(ScheduledExecutorService executor, LoaderApplication loaderapp) throws Exception {
		this.executor = executor;
		this.loaderapp = loaderapp; 
		this.loaderDao = new LoaderDao();
		
	}
	
	
	@Override
	public void run() {
		
		    try { 
				Deque<Sip> q = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
				if (q.size() == 0) q = loaderapp.getSipsByState(STATE.INGESTING);
				
				for (Sip sip : q) {
					String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
					String state = loaderapp.getSipLifeCycle(sip.getSipId());
					if(logger.isDebugEnabled()) {
						logger.debug(sip.toString());
					}
					if (state.equals(STATE.INGESTING.name())) {
						sip.setState(STATE.INGESTING);
					} else if (state.equals(STATE.INGESTED.name())) { 
						sip.setState(STATE.INGESTED);
					} else if (state.equals(STATE.FAILED.name())) {
						sip.setState(STATE.FAILED);
					}
					
					loaderDao.updateSip(sip);
				}
				
				logger.info("Retrieval of Lifecycle objects " + q.size());
				
				
		    } catch (Exception e) { 
		    	e.printStackTrace();
		    	executor.shutdownNow();
		    }
		    
		    
			
		    
		   

	}

}
