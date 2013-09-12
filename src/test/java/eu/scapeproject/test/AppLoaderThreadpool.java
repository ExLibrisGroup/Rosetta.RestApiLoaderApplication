package eu.scapeproject.test;

import static org.junit.Assert.assertFalse;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.scapeproject.ConnectorAPIMock;
import eu.scapeproject.LoaderApplication;
import eu.scapeproject.LoaderDao;
import eu.scapeproject.Sip;
import eu.scapeproject.Sip.STATE;
import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.TestUtil;
import eu.scapeproject.pt.main.Configuration;
import eu.scapeproject.pt.threads.LifecycleRunnable;
import eu.scapeproject.pt.threads.StopLifecycelTask;
import eu.scapeproject.util.ScapeMarshaller;

public class AppLoaderThreadpool {
	
	
	private static final ConnectorAPIMock MOCK = new ConnectorAPIMock(8387);
	private static LoaderApplication loaderApplication;
	private static Configuration conf;


    @BeforeClass
    public static void setup() throws Exception {
    	PropertyConfigurator.configure("log4j.properties");
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
        loaderApplication.cleanQueue();
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
    	
		
		for (int i=0; i<100; i++) {
			String sipFileName = "sips/mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=TestUtil.createTestEntity("mets_entity" + i);
			FileOutputStream out=new FileOutputStream(xmlFile);
			ScapeMarshaller.newInstance().serialize(entity, out);
			loaderApplication.enqueuSip(URI.create("file:" + sipFileName));
		}

		loaderApplication.ingestIEs();
    	
    }
    
	
	@Test
	public void testLifecyclepool() throws Exception { 
		
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
		Runnable worker = new LifecycleRunnable(scheduler, loaderApplication, "10");
		// über command line interface konfigurierbar machen
		long initialDelay = 10;
		long period = 5;
		ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(worker, initialDelay, period, TimeUnit.SECONDS);
		// this is just a mad shutdown thread after 30 seconds - nothing real 
		// condition should be: all objects are ingested
		long  shutdownDelay = 30;
		Runnable stopLCCheck = new StopLifecycelTask(future, scheduler, loaderApplication);
		ScheduledFuture<?> stopFuture = scheduler.schedule(stopLCCheck, shutdownDelay, TimeUnit.SECONDS);
        System.out.println("stop LC task after: : " + shutdownDelay + " seconds " + stopFuture.get());
		
	}

}
