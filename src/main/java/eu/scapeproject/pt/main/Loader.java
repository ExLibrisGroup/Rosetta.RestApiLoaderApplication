package eu.scapeproject.pt.main;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import eu.scapeproject.Sip.STATE;
import eu.scapeproject.pt.auth.EsciDocAuthentication;
import eu.scapeproject.pt.auth.IAuthentication;
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
 * -p,--period <arg>    Period to fetch lifecycle states [default: 1 min].
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
			LoaderApplication loaderapp = new LoaderApplication(conf, new EsciDocAuthentication(conf));
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
					
				// retrieve lifecycle state as a scheduled task
				if (conf.getChecklifecycle()=="true") {
				  logger.info("Retrieve lifecycle states in " + conf.getPeriod() + " minutes - please be patient.");
				  logger.info("System will be shutdown automatically after " + 10*Integer.parseInt(conf.getPeriod()) + " minutes.");

					ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
					// the period depends on the number of objects.
					ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new LifecycleRunnable(scheduler, loaderapp, conf.getPeriod()), Integer.parseInt(conf.getPeriod()),Integer.parseInt(conf.getPeriod()), TimeUnit.MINUTES);
					future.get();
				
					ScheduledFuture<?> stop = scheduler.schedule(new StopLifecycelTask(future,scheduler,loaderapp), 10*Integer.parseInt(conf.getPeriod()), TimeUnit.MINUTES);
					stop.get();
				}
			} else {
				System.out.println("Empty directory. No SIPs to process");
			}
			
			loaderapp.shutdown();
			System.exit(0);
			
		} catch (Exception e) {
			
			e.printStackTrace();
		} 
	   }
	
	 }
	

}
