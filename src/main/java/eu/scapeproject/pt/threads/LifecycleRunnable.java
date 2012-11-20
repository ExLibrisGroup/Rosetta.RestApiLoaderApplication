package eu.scapeproject.pt.threads;

import java.net.URLEncoder;
import java.util.Deque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;


public class LifecycleRunnable implements Runnable {

	private ScheduledExecutorService executor; 
	private LoaderApplication loaderapp; 
	static final boolean DONT_INTERRUPT_IF_RUNNING = true; 
	
	public LifecycleRunnable(ScheduledExecutorService executor, LoaderApplication loaderapp) throws Exception {
		this.executor = executor;
		this.loaderapp = loaderapp; 
	}
	
	
	@Override
	public void run() {
		
		    try { 
				Deque<Sip> q = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
				
				for (Sip sip : q) {
					String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
					loaderapp.getSipLifeCycle(id);
				}
				
				System.out.println("Retrieval of Lifecycle objects " + q.size());
				
				if (q.size() == 0) { 
					executor.shutdown();
				}
				
		    } catch (Exception e) { 
		    	e.printStackTrace();
		    }

	}

}
