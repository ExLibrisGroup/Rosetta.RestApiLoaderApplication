package eu.scapeproject.pt.threads;

import java.net.URLEncoder;
import java.util.Deque;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.LoaderDao;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;


public class LifecycleRunnable implements Runnable {

	private ScheduledExecutorService executor;
	private LoaderApplication loaderapp;
	private LoaderDao loaderDao;
	private String period;
	static final boolean DONT_INTERRUPT_IF_RUNNING = true;


	private static Logger logger =  Logger.getLogger(LifecycleRunnable.class.getName());

	public LifecycleRunnable(ScheduledExecutorService executor, LoaderApplication loaderapp, String period) throws Exception {
		this.executor = executor;
		this.loaderapp = loaderapp;
		this.loaderDao = new LoaderDao();
		this.period = period;

	}


	@Override
	public void run() {

	    try {
			Deque<Sip> q = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
			Deque<Sip> submitted = q;
			if (q.size() == 0) q = loaderapp.getSipsByNotState(STATE.INGESTED);
			logger.info("Retrieval of lifecycle states started....");
			for (Sip sip : q) {
				String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
				String state = loaderapp.getSipLifeCycle(id);
				if(logger.isDebugEnabled()) {
					logger.debug(sip.toString());
				}
				if (state.equals(STATE.INGESTING.name())) {
					sip.setState(STATE.INGESTING);
				} else if (state.equals(STATE.INGESTED.name())) {
					sip.setState(STATE.INGESTED);
				} else if (state.equals(STATE.FAILED.name())) {
					sip.setState(STATE.FAILED);
				} else if (state.equals(STATE.IN_PROGRESS.name())) {
					sip.setState(STATE.IN_PROGRESS);
				} else if (state.equals(STATE.PENDING.name())) {
					sip.setState(STATE.PENDING);
				} else if (state.equals(STATE.OTHER.name())) {
					sip.setState(STATE.OTHER);
				}

				loaderDao.updateSip(sip);
			}

			// Report Generation
			//Deque<Sip> submitted = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
			Deque<Sip> ingested = loaderapp.getSipsByState(STATE.INGESTED);
			Deque<Sip> ingesting = loaderapp.getSipsByState(STATE.INGESTING);
			Deque<Sip> failed = loaderapp.getSipsByState(STATE.FAILED);
			Deque<Sip> inprogress = loaderapp.getSipsByState(STATE.IN_PROGRESS);
			Deque<Sip> pending = loaderapp.getSipsByState(STATE.PENDING);
			Deque<Sip> other = loaderapp.getSipsByState(STATE.OTHER);

			logger.info("Submitted to Repository: " + submitted.size());
			logger.info("Ingested: " + ingested.size());
			logger.info("Ingesting: " + ingesting.size());
			logger.info("Failed: " + failed.size());
			logger.info("Other: " + other.size());
			//logger.info("Inprogress: " + inprogress.size());
			int _pending = 0;
			if (submitted.size() >= ingested.size()) {
				 _pending = submitted.size()-ingested.size();
			} else {
				_pending = submitted.size();
			}

			logger.info("Pending: " + Integer.toString(_pending));
			logger.info("Next retrieve of lifecycle states in " + period + " minutes - please be patient.");

			if (logger.isDebugEnabled()) {
				if (submitted.size() > 0) {
					logger.debug("Listing of submitted SIPs");
					for (Sip sip : submitted) {
						logger.debug("\t SUBMITTED "+ sip.getSipId());
					}
				}

				if (ingested.size() > 0) {
					logger.debug("Listing of ingested SIPs");
					for (Sip sip : ingested) {
						logger.debug("\t INGESTED "+ sip.getSipId());
					}
				}

				if (failed.size() > 0) {
					logger.debug("Listing of failed SIPs");
					for (Sip sip : failed) {
						logger.debug("\t FAILED "+ sip.getSipId());
					}
				}

				if (inprogress.size() > 0) {
					logger.debug("Listing of in_progress SIPs");
					for (Sip sip : inprogress) {
						logger.debug("\t IN PROGRESS "+ sip.getSipId());
					}
				}

				if (pending.size() > 0) {
					logger.debug("Listing of pending SIPs");
					for (Sip sip : pending) {
						logger.debug("\t PENDING "+ sip.getSipId());
					}
				}
			}
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	executor.shutdownNow();
	    }
	}

}
