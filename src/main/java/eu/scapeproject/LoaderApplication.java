package eu.scapeproject;

import java.net.URI;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import eu.scapeproject.Sip.STATE;


public class LoaderApplication {

    private URI repoURI;;
    private Deque<Sip> sipQueue;
    private LoaderDao loaderDao;

    public LoaderApplication (URI repoURI) throws Exception {
    	loaderDao = new LoaderDao();
    	this.repoURI = repoURI;
    	this.sipQueue = new LinkedList<Sip>();
    }

    public void enqueuSip(URI uri) throws SQLException {
    	Sip sip = new Sip();
    	sip.setState(STATE.PENDING);
    	sip.setUri(uri);
    	loaderDao.insertSip(sip);
    	sipQueue.add(sip);
    }

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
				HttpPost post = new HttpPost(repoURI.toASCIIString());
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

    public void shutdown() throws SQLException {
    	loaderDao.closeStatments();
    	loaderDao.shutdown();
    }

    public void cleanQueue() throws SQLException {
    	loaderDao.deleteSacpeSips();
    }
}
