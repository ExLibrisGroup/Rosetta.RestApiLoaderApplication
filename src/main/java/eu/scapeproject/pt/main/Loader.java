package eu.scapeproject.pt.main;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Deque;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;

import eu.scapeproject.pt.main.Options;
import org.apache.commons.cli.PosixParser;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;

/**
 * * Utility command line application for the SCAPE Loader Application.
 * usage: {hadoop jar|java -jar}
 *       target/tb-lsdr-seqfileutility-0.1-SNAPSHOT-jar-with-dependencies.ja
 *       r [-c <arg>] [-d <arg>] [-e <arg>] [-h] [-m] [-t]
 * -d,--dir <arg>     Local input directory (Required)
 * -u,--url <arg>     base URL of the repository (Required).
 * -h,--help          print this message.
 * -i,--ingest        ingest REST endpoint [default: entity-async] (Optional).
 * -l,--lifecycle     lifecycle REST endpoint [default: lifecycle] (Optional).
 *                    
 * @author mhn
 *
 */
public class Loader {

	public static void main(String[] args) throws ParseException {
		
		CommandLineParser cmdParser = new PosixParser();
		CommandLine cmd = cmdParser.parse(Options.OPTIONS, args);
		Configuration conf = new Configuration();
		
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
				
				// retrieve lifecycle state
				Deque<Sip> q = loaderapp.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
				
				for (Sip sip : q) {
					String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
					loaderapp.getSipLifeCycle(id);
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
