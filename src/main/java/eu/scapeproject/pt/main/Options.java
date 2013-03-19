package eu.scapeproject.pt.main;



import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.Logger;



/**
 * 
 * @author mhn
 *
 */
public class Options  {
	
	    // Logger instance
	    private static Logger logger =  Logger.getLogger(Options.class.getName());
	    // Statics to set up command line arguments
	    public static final String HELP_FLG = "h";
	    public static final String HELP_OPT = "help";
	    public static final String HELP_OPT_DESC = "print this message.";
	    public static final String USAGE = "java -jar loader-app-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
	    
	    public static final String DIR_FLG = "d";
	    public static final String DIR_OPT = "dir";
	    public static final String DIR_OPT_DESC = "Local input directory (Required). If a sequence file or a zip-file is given, an extraction into a local sips directory will be performed";
	    
	    public static final String URL_FLG = "r";
	    public static final String URL_OPT = "url";
	    public static final String URL_OPT_DESC = "base URL of the repository (Required).";
	    
	    public static final String INGEST_FLG = "i";
	    public static final String INGEST_OPT = "ingest";
	    public static final String INGEST_OPT_DESC = "ingest REST endpoint [default: entity-async].";
	    
	    public static final String LIFECYCLE_FLG = "l";
	    public static final String LIFECYCLE_OPT = "lifecycle";
	    public static final String LIFECYCLE_OPT_DESC = "lifecycle REST endpoint [default: lifecycle].";
	    
	    public static final String PERIOD_FLG = "t";
	    public static final String PERIOD_OPT = "period";
	    public static final String PERIOD_OPT_DESC = "Period in min to fetch lifecycle states [default: 100 min].";
	    
	    public static final String USER_FLG = "u";
	    public static final String USER_OPT = "username";
	    public static final String USER_OPT_DESC = "username of the repository user.";
	    
	    public static final String PASSWORD_FLG = "p";
	    public static final String PASSWORD_OPT = "password";
	    public static final String PASSWORD_OPT_DESC = "password of the repository user.";
	    
	    public static final String CHECKLIFECYCLE_FLG = "c";
	    public static final String CHECKLIFECYCLE_OPT = "checklifecycle";
	    public static final String CHECKLIFECYCLE_OPT_DESC = "activate the periodic lifecycle retrieval. [default: true]";
	    
	    // Static for command line option parsing
	    public static org.apache.commons.cli.Options OPTIONS = new org.apache.commons.cli.Options();
	   

	    static {
	        OPTIONS.addOption(HELP_FLG, HELP_OPT, false, HELP_OPT_DESC);
	        OPTIONS.addOption(DIR_FLG, DIR_OPT, true, DIR_OPT_DESC);
	        OPTIONS.addOption(URL_FLG, URL_OPT, true, URL_OPT_DESC);
	        OPTIONS.addOption(INGEST_FLG, INGEST_OPT, true, INGEST_OPT_DESC);
	        OPTIONS.addOption(LIFECYCLE_FLG, LIFECYCLE_OPT, true, LIFECYCLE_OPT_DESC);
	        OPTIONS.addOption(PERIOD_FLG, PERIOD_OPT, true, PERIOD_OPT_DESC);
	        OPTIONS.addOption(USER_FLG, USER_OPT, true, USER_OPT_DESC);
	        OPTIONS.addOption(PASSWORD_FLG, PASSWORD_OPT, true, PASSWORD_OPT_DESC);
	        OPTIONS.addOption(CHECKLIFECYCLE_FLG, CHECKLIFECYCLE_OPT, true, CHECKLIFECYCLE_OPT_DESC);
	    }
	    
	    
	    public static void initOptions(CommandLine cmd, Configuration conf) {

	        String dirStr;
	        String urlStr;
	        String ingestStr;
	        String lifecycleStr;
	        String periodStr;
	        String usernameStr;
	        String passwordStr;
	        String checklifecycleStr;

	        // dirs
	        if (!(cmd.hasOption(DIR_OPT) && cmd.getOptionValue(DIR_OPT) != null)) {
	            exit("No directory given.", 1);
	        } else {
	            dirStr = cmd.getOptionValue(DIR_OPT);
	            conf.setDir(dirStr);
	            logger.info("Directory: " + dirStr);
	        }
	        
	        // url
	        if (!(cmd.hasOption(URL_OPT) && cmd.getOptionValue(URL_OPT) != null)) {
	            exit("No URL given.", 1);
	        } else {
	            urlStr = cmd.getOptionValue(URL_OPT);
	            conf.setUrl(urlStr);
	            logger.info("Base URL: " + urlStr);
	        }
	        
	        // Rest end points
	        if (!(cmd.hasOption(INGEST_OPT) && cmd.getOptionValue(INGEST_OPT) != null)) {
	        	conf.setIngest("entity-async");
	        } else {
	            ingestStr = cmd.getOptionValue(INGEST_OPT);
	            conf.setIngest(ingestStr);
	            logger.info("INGEST REST end point: " + ingestStr);
	        }
	        
	        if (!(cmd.hasOption(LIFECYCLE_OPT) && cmd.getOptionValue(LIFECYCLE_OPT) != null)) {
	        	conf.setLifecycle("lifecycle");
	        } else {
	            lifecycleStr = cmd.getOptionValue(LIFECYCLE_OPT);
	            conf.setLifecycle(lifecycleStr);
	            logger.info("LIFECYCLE REST end point: " + lifecycleStr);
	        }
	        
	        if (!(cmd.hasOption(PERIOD_OPT) && cmd.getOptionValue(PERIOD_OPT) != null)) {
	        	conf.setPeriod("100");
	        } else {
	            periodStr = cmd.getOptionValue(PERIOD_OPT);
	            conf.setPeriod(periodStr);
	            logger.info("Period in min to fetch lifecycle states: " + periodStr);
	        }
	        
	        if (!(cmd.hasOption(USER_OPT) && cmd.getOptionValue(USER_OPT) != null)) {
	        	exit("No username given.", 1);
	        } else {
	            usernameStr = cmd.getOptionValue(USER_OPT);
	            conf.setUser(usernameStr);
	            logger.info("username: " + usernameStr);
	        }
	        
	        if (!(cmd.hasOption(PASSWORD_OPT) && cmd.getOptionValue(PASSWORD_OPT) != null)) {
	        	exit("No password given.", 1);
	        } else {
	            passwordStr = cmd.getOptionValue(PASSWORD_OPT);
	            conf.setPassword(passwordStr);
	            logger.info("password: " + passwordStr);
	        }
	        
	        
	        if (!(cmd.hasOption(CHECKLIFECYCLE_OPT) && cmd.getOptionValue(CHECKLIFECYCLE_OPT) != null)) {
	        	conf.setChecklifecycle("true");
	        } else {
	        	checklifecycleStr = cmd.getOptionValue(CHECKLIFECYCLE_OPT);
	            conf.setChecklifecycle(checklifecycleStr);
	            logger.info("CHECKLIFECYCLE: " + checklifecycleStr);
	        }
	        

	    }
	    
	    
	    
	    public static void exit(String msg, int status) {
	        if (status > 0) {
	            logger.error(msg);
	        } else {
	            logger.info(msg);
	        }
	        HelpFormatter formatter = new HelpFormatter();
	        formatter.printHelp(USAGE, OPTIONS, true);
	        System.exit(status);
	    }



}
