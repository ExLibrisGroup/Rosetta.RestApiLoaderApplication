package eu.scapeproject;

import java.net.URI;
import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.mortbay.jetty.HttpStatus;

import sun.misc.BASE64Encoder;
import eu.scapeproject.Sip.STATE;
import eu.scapeproject.pt.auth.IAuthentication;
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
    private DefaultHttpClient httpclient;
    private IAuthentication auth;

    private static Logger logger =  Logger.getLogger(LoaderApplication.class.getName());

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

    public LoaderApplication(Configuration conf, IAuthentication auth) throws Exception {
    	loaderDao = new LoaderDao();
    	this.repoURI = new URI(conf.getUrl());
    	this.sipQueue = new LinkedList<Sip>();
    	this.conf = conf;
    	this.auth = auth;
    	this.httpclient = auth.logon();
    }

    public LoaderApplication(Configuration conf) throws Exception {
    	loaderDao = new LoaderDao();
    	this.repoURI = new URI(conf.getUrl());
    	this.sipQueue = new LinkedList<Sip>();
    	this.conf = conf;
    	this.httpclient = new DefaultHttpClient();
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
    public void ingestIEs(Configuration conf) throws Exception {

    	sipQueue = loaderDao.getAllSipsByState(STATE.PENDING);
    	long starttime = System.currentTimeMillis();
    	int objects =  sipQueue.size();

    	while (!sipQueue.isEmpty()) {
    		Sip sip = null;
			String sipId = null;

			try {
				sip = sipQueue.pop();
				sip.setState(STATE.IN_PROGRESS);
				loaderDao.updateSip(sip);

				String encoding = new BASE64Encoder().encode((conf.getUser() + ":" + conf.getPassword()).getBytes());
				StringEntity stringEntity = new StringEntity(IOUtils.toString(sip.getUri().toURL().openStream()));
				HttpPost post = new HttpPost(repoURI.toASCIIString() + "/" + conf.getIngest());
				post.setHeader("Authorization", "Basic " + encoding);
				post.setEntity(stringEntity);

				HttpResponse resp = httpclient.execute(post);

				if (logger.isDebugEnabled()) {
					List<Cookie> cookies = httpclient.getCookieStore().getCookies();
					for (Cookie cookie : cookies) {
						logger.debug(cookie.getName() + ".." + cookie.getValue());
					}
				}

				String ist = IOUtils.toString(resp.getEntity().getContent());
				if (resp.getStatusLine().getStatusCode() != HttpStatus.ORDINAL_200_OK) {
		    		ist = "Error: " + "  RETURN CODE: " + resp.getStatusLine().getStatusCode() + " " + resp.getStatusLine().getReasonPhrase() + "\n" + ist;
		    	} else {
					sipId = extractSipId(ist);
		    	}
				if (sipId != null) {
					sip.setSipId(sipId);
					sip.setState(STATE.SUBMITTED_TO_REPOSITORY);
					logger.info("Return Code: " + resp.getStatusLine().getStatusCode() + " SIP ID: " + sipId);
				} else {
					logger.info("SIP ID is NULL. Is repository up and running? Error Message:" + ist);
				}
				post.releaseConnection();

			} catch (Exception e) {
				e.printStackTrace();
				sip.setState(STATE.FAILED);
			}

    		loaderDao.updateSip(sip);
    	}

    	long elapsedtime = System.currentTimeMillis() - starttime;
    	System.out.println("Ingest of "+ objects + " objects finsihed. Total elapsed time: " + elapsedtime + " ms." + " Average time per object: " + elapsedtime/objects + " ms");
    }

    /**
     * retrieve the life cycle of a sip using its id
     * @param entityId
     * @throws Exception
     */
    public String getSipLifeCycle(String entityId) throws Exception {
    	String encoding = new BASE64Encoder().encode((conf.getUser() + ":" + conf.getPassword()).getBytes());
    	String uri = repoURI.toASCIIString() + "/" + conf.getLifecycle() +"/" + entityId;
    	HttpGet get = new HttpGet(uri);
    	get.setHeader("Authorization", "Basic " + encoding);
    	if(logger.isDebugEnabled()) {
    		logger.debug(get.toString()) ;
    	}
    	HttpResponse resp = httpclient.execute(get);
    	if (logger.isDebugEnabled()) {
	    	List<Cookie> cookies = httpclient.getCookieStore().getCookies();
			for (Cookie cookie : cookies) {
				logger.debug(cookie.getName() + ".." + cookie.getValue());
			}
    	}

    	String out = null;
    	if (resp.getStatusLine().getStatusCode() != HttpStatus.ORDINAL_200_OK) {
    		out = "Error: " + "  RETURN CODE: " + resp.getStatusLine().getStatusCode() + " " + resp.getStatusLine().getReasonPhrase();
    	}
    	else {
    		out = extractLifecyclestate(IOUtils.toString(resp.getEntity().getContent()));
    	}
    	if (logger.isDebugEnabled()) {
    		logger.debug("ID: " + entityId + " STATUS: " + out + "  RETURN CODE: " + resp.getStatusLine().getStatusCode() + " " + resp.getStatusLine().getReasonPhrase());
    	}
    	get.releaseConnection();
    	return out;
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


   /**
    * added to extract the sipId form the response
    * @param response
    * @return
    */
    private String extractSipId(String response) {
// fuer Fedora 4 geanedert da die sipid nicht in den tags gewrapped zurück kommt
//    	String begin = "<scape:value>";
//		String end = "</scape:value>";
//		int beginIndex = response.indexOf(begin);
//		int endIndex = response.indexOf(end);
//		String result = null;
//		if (beginIndex > -1 && endIndex > -1) {
//			result = response.substring(beginIndex+begin.length(), endIndex);
//		}
    	String result = response;
		return result;
    }

    /**
     *  a helper to extract the lifecycle state out of the response
     * @param response
     * @return
     */
    private String extractLifecyclestate(String response) {
    	Pattern pattern = Pattern.compile("lifecyclestate=.*\">");
		Matcher matcher = pattern.matcher(response);
		String result = STATE.SUBMITTED_TO_REPOSITORY.name();
		while (matcher.find()) {
		     String[] x = matcher.group().split("=");
		     if(x.length > 1) {
		       result = x[1].substring(1, x[1].length()-2);
		     }
		}

		return result;
    }

    public Deque<Sip> getSipsByState(STATE state) throws SQLException {
    	return loaderDao.getAllSipsByState(state);
    }

    public Deque<Sip> getSipsByNotState(STATE state) throws SQLException {
    	return loaderDao.getAllSipsByNotState(state);
    }
}
