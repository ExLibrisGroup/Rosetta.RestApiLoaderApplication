package eu.scapeproject.pt.threads;

import java.sql.SQLException;
import java.util.Deque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;

/**
 * Stops the Lifecycle Thread after a certain condition (not yet implemented just a period of time currently) 
 * @author mhn
 *
 */
public class StopLifecycelTask implements Runnable {

	private ScheduledFuture<?> stopFuture;
	private ScheduledExecutorService executor; 
	private LoaderApplication loaderapp; 
	static final boolean DONT_INTERRUPT_IF_RUNNING = true;

	
	public StopLifecycelTask(ScheduledFuture<?> future, ScheduledExecutorService executor, LoaderApplication loaderapp) { 
		this.stopFuture = future;
		this.executor = executor;
		this.loaderapp = loaderapp;
	}
	
	
	@Override
	public void run() {
		
		try {
			
			Deque<Sip> q = loaderapp.getSipsByState(STATE.PENDING);
			if(q.size() == 0 || q.isEmpty()) { 
			 stopFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
		     executor.shutdown();
			}
		      
		} catch (SQLException e) {
			e.printStackTrace();
		}
		 
	}

}
