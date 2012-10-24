package eu.scapeproject.test;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;

/**
 *
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

	/**
	 * Uses the scape-dto project to create 100 random SIPs and enqueue them into the loader application queue.
	 * The test activates the loader application ingestIEs function and verify that the ingest process has been completed without errors.
	 * @throws Exception
	 */
	@Test
	public void testIngestIEs() throws Exception {
		LoaderApplication loaderApplication = new LoaderApplication(URI.create(baseUrl));
		loaderApplication.cleanQueue();

		for (int i=0; i<100; i++) {
			String sipFileName = "sips/mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=TestUtil.createRandomEntity();
			FileOutputStream out=new FileOutputStream(xmlFile);
			SCAPEMarshaller.getInstance().serialize(entity, out);
			loaderApplication.enqueuSip(URI.create("file:" + sipFileName));
		}

		loaderApplication.ingestIEs();
		loaderApplication.shutdown();
	}

	/**
	 * Randomize an IE id and call the loader application getSipLifeCycle function.
	 * @throws Exception
	 */
	@Test
	public void testlifeCycle() throws Exception {
		LoaderApplication loaderApplication = new LoaderApplication(URI.create(baseUrl));
		loaderApplication.getSipLifeCycle("IE-" + Math.abs(new Random().nextInt()));
		loaderApplication.shutdown();
	}

	@BeforeClass
	public static void initServletContainer() throws Exception {
		tester = new ServletTester();
		tester.setContextPath("/");
		tester.addServlet(EntitySyncServlet.class, "/entity-async");
		tester.addServlet(EntityLifecycleServlet.class, "/lifecycle");
		baseUrl = tester.createSocketConnector(true);
		tester.start();
	}


	@AfterClass
	public static void cleanupServletContainer() throws Exception {
		tester.stop();
	}
}
