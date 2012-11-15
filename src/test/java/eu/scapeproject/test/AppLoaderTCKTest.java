package eu.scapeproject.test;

import static org.junit.Assert.assertFalse;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Deque;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import eu.scapeproject.ConnectorAPIMock;
import eu.scapeproject.LoaderApplication;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;
import eu.scapeproject.pt.main.Configuration;
import eu.scapeproject.pt.main.LoaderIO;


/**
 * Test with the TCK instead of the Jetty(servlet) solution used with AppLoaderTests
 * @author mhn
 *
 */
public class AppLoaderTCKTest {
	
	private static String baseUrl = "http://localhost:8387";
	private static final ConnectorAPIMock MOCK = new ConnectorAPIMock(8387);
	private static LoaderApplication loaderApplication;
	private static Configuration conf;


    @BeforeClass
    public static void setup() throws Exception {
        Thread t = new Thread(MOCK);
        t.start();
        while (!MOCK.isRunning()) {
            Thread.sleep(10);
        }
        
        conf = new Configuration(); 
        conf.setDir("sips/");
        conf.setUrl("http://localhost:8387");
        conf.setIngest("entity-async");
        conf.setLifecycle("lifecycle");
        loaderApplication =  new LoaderApplication(conf);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MOCK.stop();
        MOCK.close();
        assertFalse(MOCK.isRunning());
        
        loaderApplication.shutdown();
    }
    
    
    @Test
    public void testIngest() throws Exception { 
    	
    	
		loaderApplication.cleanQueue();

		LoaderIO io = new LoaderIO(); 
		
		for (int i=0; i<100; i++) {
			String sipFileName = "sips/mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=TestUtil.createRandomEntity();
			FileOutputStream out=new FileOutputStream(xmlFile);
			SCAPEMarshaller.getInstance().serialize(entity, out);
			loaderApplication.enqueuSip(URI.create("file:" + sipFileName));
		}

		loaderApplication.ingestIEs();
    	
    }
    
    @Test
	public void testlifeCycle() throws Exception {
		
		Deque<Sip> q = loaderApplication.getSipsByState(STATE.SUBMITTED_TO_REPOSITORY);
		
		for (Sip sip : q) {
			String id = URLEncoder.encode(sip.getSipId().trim(), "UTF-8");
			loaderApplication.getSipLifeCycle(id);
		}
		
		
		
	}

}
