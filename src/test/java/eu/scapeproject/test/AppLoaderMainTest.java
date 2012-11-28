package eu.scapeproject.test;

import static org.junit.Assert.assertFalse;

import java.io.FileNotFoundException;
import javax.xml.bind.JAXBException;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SerializationException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import eu.scapeproject.ConnectorAPIMock;
import eu.scapeproject.pt.main.Loader;
import eu.scapeproject.pt.main.LoaderIO;

/**
 * Test of the Loader Class using the TCK
 * @author mhn
 *
 */

public class AppLoaderMainTest {
	
	private static final ConnectorAPIMock MOCK = new ConnectorAPIMock(8387);
	private static final int numberOfSips = 100;
	
	@BeforeClass
    public static void setup() throws Exception {
        Thread t = new Thread(MOCK);
        t.start();
        while (!MOCK.isRunning()) {
            Thread.sleep(10);
        }  
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MOCK.stop();
        MOCK.close();
        assertFalse(MOCK.isRunning());
    }
    
    @Test
    public void testSipGeneration()  { 
    	try {
    		LoaderIO io = new LoaderIO(); 
			io.generateSips(numberOfSips);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SerializationException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
    }
	
	
	@Test
	public void testMain() {  
		String[] args = {"-d /home/mhn/git/loader-app/sips", "-u http://localhost:8387"};
		//String[] args = {"http://localhost:8080","/home/mhn/git/loader-app/sips"};
		try {
			Loader.main(args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
