package eu.scapeproject.test;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.Random;

import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;
import eu.scapeproject.pt.main.Configuration;
import eu.scapeproject.util.ScapeMarshaller;


/**
 * @author Shai Ben-Hur
 *
 * Prior to the test run this class will load embedded Jetty (HTTP server) that will be used as a servelt container emulating the repository application.
 * This class contain 2 main unit tests:
 * 1. Ingest IEs test
 * 2. Get IEs lifeCycle test
 *
 */
public class AppLoaderTests {

	private static ServletTester tester;
	private static String baseUrl;
	private static Configuration conf;
	
	/**
	 * Uses the scape-dto project to create 100 random SIPs and enqueue them into the loader application queue.
	 * The test activates the loader application ingestIEs function and verify that the ingest process has been completed without errors.
	 * Each sip will be posted by the loader application to the EntitySyncServlet which will print the IE id and return a random sip id.
	 * @throws Exception
	 */
	@Test
	public void testIngestIEs() throws Exception {
		
		LoaderApplication loaderApplication = new LoaderApplication(conf);
		loaderApplication.cleanQueue();

		for (int i=0; i<100; i++) {
			String sipFileName = conf.getDir() + "mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=eu.scapeproject.model.TestUtil.createTestEntity("mets_entity_" + i );
			FileOutputStream out=new FileOutputStream(xmlFile);
			ScapeMarshaller.newInstance().serialize(entity, out);
			loaderApplication.enqueuSip(URI.create("file:" + sipFileName));
		}

		loaderApplication.ingestIEs();
		loaderApplication.shutdown();
	}

	/**
	 * Randomize an IE id and call the loader application getSipLifeCycle function.
	 * A permanent xml will be return from the EntityLifecycleServlet
	 * 
	 * Please use AppLoaderTCKTest instead
	 * 
	 * @throws Exception
	 */

	@Deprecated
	public void testlifeCycle() throws Exception {
		LoaderApplication loaderApplication = new LoaderApplication(URI.create(baseUrl));
		loaderApplication.getSipLifeCycle("IE-" + Math.abs(new Random().nextInt()));
		loaderApplication.shutdown();
	}

	@BeforeClass
	public static void initServletContainer() throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		// Configuration of the job run
		conf = new Configuration(); 
        conf.setDir("sips/");
        conf.setIngest("entity-async");
        conf.setLifecycle("lifecycle");
		
		tester = new ServletTester();
		tester.setContextPath("/");
		tester.addServlet(EntitySyncServlet.class, "/"+ conf.getIngest());
		tester.addServlet(EntityLifecycleServlet.class, "/" + conf.getLifecycle());
		baseUrl = tester.createSocketConnector(true);
		conf.setUrl(baseUrl);
		tester.start();
	}

	@AfterClass
	public static void cleanupServletContainer() throws Exception {
		tester.stop();
	}
}