package eu.scapeproject.pt.main;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import eu.scapeproject.Sip.STATE;
import eu.scapeproject.pt.main.Options;
import eu.scapeproject.pt.threads.LifecycleRunnable;
import eu.scapeproject.pt.threads.StopLifecycelTask;

import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import eu.scapeproject.LoaderApplication;
import eu.scapeproject.Sip;


/**
 * * Utility command line application for the SCAPE Loader Application.
 * usage: {java -jar}
 *       target/loader-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar [-d
       <arg>] [-h] [-i <arg>] [-l <arg>] [-u <arg>]
 * -d,--dir <arg>     Local input directory (Required)
 * -u,--url <arg>     base URL of the repository (Required).
 * -h,--help          print this message.
 * -i,--ingest  <arg>      ingest REST endpoint [default: entity-async] (Optional).
 * -l,--lifecycle <arg>    lifecycle REST endpoint [default: lifecycle] (Optional).
 *                    
 * @author mhn
 *
 */
public class Loader {
	
	private static Logger logger =  Logger.getLogger(Loader.class.getName());

	public static void main(String[] args) throws ParseException {
		
		CommandLineParser cmdParser = new PosixParser();
		CommandLine cmd = cmdParser.parse(Options.OPTIONS, args);
		Configuration conf = new Configuration();
		PropertyConfigurator.configure("log4j.properties");
		
		if ((args.length == 0) || (cmd.hasOption(Options.HELP_OPT))) {
            Options.exit("Usage", 0);
        } else {
            Options.initOptions(cmd, conf);
		
		try {
			LoaderApplication loaderapp = new LoaderApplication(conf);
			loaderapp.cleanQueue();
			
			// fetch sips from directory
			LoaderIO io = new LoaderIO(); 
			String[] files = io.getFiles(conf.getDir());
			if (files != null  && files.length > 0) { 
				for (int i = 0; i < files.length; i++) {
					loaderapp.enqueuSip(URI.create("file:" + conf.getDir() +  files[i]));
				}
				
				// ingest
				loaderapp.ingestIEs();	
				
				if (conf.getMode()) { 
					
					// retrieve lifecycle state as a scheduled task
					logger.info("Retrieve Lifecycle states every " + 2*files.length + " seconds - shutdown after " + 5*files.length + " seconds");
					ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
					Runnable worker = new LifecycleRunnable(scheduler, loaderapp);
					long initialDelay = 10;
					// the period depends on the number of objects.
					long period = 2*files.length;
					ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(worker, initialDelay, period, TimeUnit.SECONDS);
	
					// this is just a shutdown thread after 60 seconds - nothing real 
					// condition should be: all objects are ingested @TODO
					long  shutdownAfter = 5*files.length;
					Runnable stopLCCheck = new StopLifecycelTask(future, scheduler, loaderapp);
					ScheduledFuture<?> stopFuture = scheduler.schedule(stopLCCheck, shutdownAfter, TimeUnit.SECONDS);
			        logger.info("stop Lifecycle task after: " + shutdownAfter + " seconds " + stopFuture.get());
			        
				} else { 
					
					Deque<Sip> q = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
					for (Sip sip : q) {
						//String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
						String id = sip.getSipId().trim();
						loaderapp.getSipLifeCycle(id);
					}
				}

			} else {
				System.out.println("Empty directory. No SIPs to process");
			}
			
			loaderapp.shutdown();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		} 
	   }
	
	 }
	

}
