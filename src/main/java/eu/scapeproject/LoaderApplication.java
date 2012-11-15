package eu.scapeproject;

import java.net.URI;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import eu.scapeproject.Sip.STATE;
import eu.scapeproject.pt.main.Configuration;

/**
 * @author Shai Ben-Hur
 *
 * The loader application uses methods that are defined as part of the Connector API for connecting to the repository and ingesting the SIP into the repository.
 * The loader application has a persistent layer that is build out of HSQLDB (simple relational database engine).
 * All changes and states of the application are saved on a simple text file (scape.db.script).
 *
 */
public class LoaderApplication {

    private URI repoURI;;
    private Deque<Sip> sipQueue;
    private LoaderDao loaderDao;
    private Configuration conf;

    /**
     * Constructor for the loader application. Initials the data access layer and sip queue.
     *
     * @param repoURI the repository URI
     * @throws Exception
     */
    public LoaderApplication (URI repoURI) throws Exception {
    	loaderDao = new LoaderDao();
    	this.repoURI = repoURI;
    	this.sipQueue = new LinkedList<Sip>();
    }
    
    public LoaderApplication(Configuration conf) throws Exception { 
    	loaderDao = new LoaderDao();
    	this.repoURI = new URI(conf.getUrl());
    	this.sipQueue = new LinkedList<Sip>();
    	this.conf = conf;
    }

    /**
     * Add a sip to the ingest queue
     * @param uri the sip URI.
     * @throws SQLException
     */
    public void enqueuSip(URI uri) throws SQLException {
    	Sip sip = new Sip();
    	sip.setState(STATE.PENDING);
    	sip.setUri(uri);
    	loaderDao.insertSip(sip);
    	sipQueue.add(sip);
    }

    /**
     * Activates the ingest process.
     * All Sips are loaded from the DB into the queue when this function is lunched.
     * @throws Exception
     */
    public void ingestIEs() throws Exception {

    	sipQueue = loaderDao.getAllSipsByState(STATE.PENDING);

    	while (!sipQueue.isEmpty()) {
    		Sip sip = null;
			String sipId = null;

			try {
				sip = sipQueue.pop();
				sip.setState(STATE.IN_PROGRESS);
				loaderDao.updateSip(sip);

				ByteArrayEntity byteArrayEntity = new ByteArrayEntity(IOUtils.toString(sip.getUri().toURL().openStream()).getBytes());
				HttpPost post = new HttpPost(repoURI.toASCIIString() + "/" + conf.getIngest());

//				HttpPost post = new HttpPost(repoURI.toASCIIString() + "/entity-async");
				post.setEntity(byteArrayEntity);
				HttpResponse resp = new DefaultHttpClient().execute(post);
//				sipId = IOUtils.toString(resp.getEntity().getContent());
				sipId = extractSipId(IOUtils.toString(resp.getEntity().getContent()));
				sip.setSipId(sipId);
				sip.setState(STATE.SUBMITTED_TO_REPOSITORY);
				System.out.println("Return Code: " + resp.getStatusLine().getStatusCode());
				post.releaseConnection();
				
			} catch (Exception e) {
				e.printStackTrace();
				sip.setState(STATE.FAILED);
			}

    		loaderDao.updateSip(sip);
    	}
    }

    /**
     * retrieve the life cycle of a sip using its id
     * @param entityId
     * @throws Exception
     */
    public void getSipLifeCycle(String entityId) throws Exception {
//    	HttpGet get = new HttpGet(repoURI.toASCIIString() + "/lifecycle?Id=" + entityId);
    	HttpGet get = new HttpGet(repoURI.toASCIIString() + "/" + conf.getLifecycle() +"/" + entityId);
    	HttpResponse resp = new DefaultHttpClient().execute(get);
    	System.out.println("RETURN CODE: " + resp.getStatusLine().getStatusCode());
    	
    	String out = IOUtils.toString(resp.getEntity().getContent());

    	get.releaseConnection();
    	System.out.println(out);
    }

    /**
     * Safety closes the DB connections.
     * @throws SQLException
     */
    public void shutdown() throws SQLException {
    	loaderDao.closeStatments();
    	loaderDao.shutdown();
    }

    /**
     * Clean the queue by deleting all the records from the DB.
     * @throws SQLException
     */
    public void cleanQueue() throws SQLException {
    	loaderDao.deleteSacpeSips();
    }
    
    
    // just a helper method - maybe better to use XPath 
    // added to extract the sipId form the response
    private String extractSipId(String response) { 
    	String begin = "<scape:value>";
		String end = "</scape:value>";
		int beginIndex = response.indexOf(begin);
		int endIndex = response.indexOf(end);
		return response.substring(beginIndex+begin.length(), endIndex);
    }
    
    public Deque<Sip> getSipsByState(STATE state) throws SQLException { 
    	return loaderDao.getAllSipsByState(state);		
    }
}
