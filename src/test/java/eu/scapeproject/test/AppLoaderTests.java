package eu.scapeproject.test;

import java.io.FileOutputStream;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.testing.ServletTester;

import eu.scapeproject.LoaderApplication;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;

public class AppLoaderTests {

	private static ServletTester tester;
	private static String baseUrl;

	@Test
	public void testPost () throws Exception {
		LoaderApplication loaderApplication = new LoaderApplication(URI.create(baseUrl + "/entity-async"));
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

	@BeforeClass
	public static void initServletContainer() throws Exception {
		tester = new ServletTester();
		tester.setContextPath("/");
		tester.addServlet(DummyRepositoryServlet.class, "/entity-async");
		baseUrl = tester.createSocketConnector(true);
		tester.start();
	}


	@AfterClass
	public static void cleanupServletContainer() throws Exception {
		tester.stop();
	}

}
