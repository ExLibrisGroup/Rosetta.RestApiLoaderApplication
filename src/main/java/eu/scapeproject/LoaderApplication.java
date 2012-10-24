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

/**
 * @author Shai Ben-Hur
 *
 * The loader application uses methods that are defined as part of the Connector API for connecting to the repository and ingesting the SIP into the repository.
 *
 */
public class LoaderApplication {

    private URI repoURI;;
    private Deque<Sip> sipQueue;
    private LoaderDao loaderDao;

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
				HttpPost post = new HttpPost(repoURI.toASCIIString() + "/entity-async");
				post.setEntity(byteArrayEntity);
				HttpResponse resp = new DefaultHttpClient().execute(post);
				sipId = IOUtils.toString(resp.getEntity().getContent());
				sip.setSipId(sipId);
				sip.setState(STATE.SUBMITTED_TO_REPOSITORY);
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
    	HttpGet get = new HttpGet(repoURI.toASCIIString() + "/lifecycle?Id=" + entityId);
    	HttpResponse resp = new DefaultHttpClient().execute(get);
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
}
