package eu.scapeproject.pt.main;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.pt.threads.LifecycleRunnable;
import eu.scapeproject.pt.threads.StopLifecycelTask;


/**
 * * Utility command line application for the SCAPE Loader Application.
 * usage: java -jar loader-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar [-c
 *      <arg>] [-d <arg>] [-h] [-i <arg>] [-l <arg>] [-p <arg>] [-r <arg>]
 *      [-t <arg>] [-u <arg>]
 *-c,--checklifecycle <arg>   activate the periodic lifecycle retrieval.
 *                            [default: true]
 *-d,--dir <arg>              Local input directory (Required). If a
 *                            sequence file or a zip-file is given, an extraction into a
 *                            local sips directory will be performed
 *-h,--help                   print this message.
 *-i,--ingest <arg>           ingest REST endpoint [default: entity-async].
 *-l,--lifecycle <arg>        lifecycle REST endpoint [default: lifecycle].
 *-p,--password <arg>         password of the repository user.
 *-r,--url <arg>              base URL of the repository (Required).
 *-t,--period <arg>           Period in min to fetch lifecycle states
 *                            [default: 100 min].
 *-u,--username <arg>         username of the repository user.
 *
 * @author mhn
 *
 */
public class Loader {

	private static Logger logger =  Logger.getLogger(Loader.class.getName());


	public static void main(String[] args) throws ParseException, IOException {

		CommandLineParser cmdParser = new PosixParser();
		CommandLine cmd = cmdParser.parse(Options.OPTIONS, args);
		Configuration conf = new Configuration();
		Properties props = new Properties();
		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("log4j.properties"));
		PropertyConfigurator.configure(props);

		if ((args.length == 0) || (cmd.hasOption(Options.HELP_OPT))) {
            Options.exit("Usage", 0);
        } else {
            Options.initOptions(cmd, conf);

		try {
			//LoaderApplication loaderapp = new LoaderApplication(conf, new EsciDocAuthentication(conf));
			LoaderApplication loaderapp = new LoaderApplication(conf);

			loaderapp.cleanQueue();

			// fetch sips from directory
			LoaderIO io = new LoaderIO();
			io.cleandir();

			// fetch sips from a sequence file or a given directory?
			if (conf.getDir().contains(".seq")) {
				logger.info("A sequence file has been identified.");
				io.extractSeqFile(conf.getDir());
				// local source dir where the seq file has been extracted to
				conf.setDir("sips/");
			} else if (conf.getDir().contains(".zip")) {
				logger.info("A zip folder has been identified.");
				io.extractZipFile(conf.getDir());
				conf.setDir("sips/");
			}

			String[] files = io.getFiles(conf.getDir());
			if (files != null  && files.length > 0) {

				for (int i = 0; i < files.length; i++) {
					loaderapp.enqueuSip(URI.create("file:" + conf.getDir().replace('\\', '/') +  files[i]));
				}

				// ingest
				loaderapp.ingestIEs(conf);

				// retrieve lifecycle state as a scheduled task
				if (conf.getChecklifecycle()=="true") {
					logger.info("Retrieve lifecycle states in " + conf.getPeriod() + " minutes - please be patient.");
					logger.info("System will be shutdown automatically after " + 5*Integer.parseInt(conf.getPeriod()) + " minutes, if no deposits are waiting.");

					ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
					ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new LifecycleRunnable(scheduler, loaderapp, conf.getPeriod()), Integer.parseInt(conf.getPeriod()),Integer.parseInt(conf.getPeriod()), TimeUnit.MINUTES);

					ScheduledFuture<?> stop = scheduler.schedule(new StopLifecycelTask(future,scheduler,loaderapp), 5*Integer.parseInt(conf.getPeriod()), TimeUnit.MINUTES);
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
