package eu.scapeproject.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.scapeproject.pt.main.Loader;

/**
 * Test to login at escidoc with HTTPClient
 * @author mhn
 *
 */
public class AppLoaderHTTPClient {
	
	private static final DefaultHttpClient httpclient = new DefaultHttpClient();
	
	@BeforeClass
	public static void setup() { 
		PropertyConfigurator.configure("log4j.properties");
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy());
	}
	
	@AfterClass
	public static void shutdown() { 
		 httpclient.getConnectionManager().shutdown();
	}
	
	
	//@Test
	public void testLogon() throws ClientProtocolException, IOException { 
		
		
		HttpGet httpget = new HttpGet("http://localhost:8080/aa/login/login.html");
        HttpResponse response = httpclient.execute(httpget);
       
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
        System.out.println("Login form get HTTP Status Code: " + response.getStatusLine().getStatusCode());
   	    
        System.out.println("Initial set of cookies:");
        List<Cookie> cookies = httpclient.getCookieStore().getCookies();
        String cookie = null;
        if (cookies.isEmpty()) {
             System.out.println("None");
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                System.out.println("- " + cookies.get(i).toString());
                cookie = cookies.get(i).getValue();
                System.out.println("COOKIE: " + cookie);
             }
         }
             
             
         System.out.println("Start Login: ");
         HttpPost httppost = new HttpPost("http://localhost:8080/aa/j_spring_security_check");
         httppost.setHeader("Referer", "http://localhost:8080/aa/login/login.html");
         httppost.setHeader("Connection", "keep-alive");
//             httppost.setHeader("Cookie","JSESSIONID="+cookie);
         httppost.getParams().setParameter("http.protocol.handle-redirects",true);
             
         List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("j_username", "sysadmin"));
         nvps.add(new BasicNameValuePair("j_password", "esciDoc"));
         nvps.add(new BasicNameValuePair("Abschicken", "submit"));

         httppost.setEntity(new UrlEncodedFormEntity(nvps, "ISO-8859-1"));
         HttpHost target = new HttpHost("localhost",8080); 
         HttpResponse postresponse = httpclient.execute(target,httppost);
         entity = postresponse.getEntity();

         System.out.println("Login form get: " + postresponse.getStatusLine().getStatusCode());
             
         EntityUtils.consume(entity);
         httppost.releaseConnection();    
         
         
         HttpGet get2 = new HttpGet("http://localhost:8080/aa/login?target=http://localhost:8080/AdminTool/");
         HttpResponse response2 = httpclient.execute(get2);  
         System.out.println("Post logon cookies:");
         cookies = httpclient.getCookieStore().getCookies();
         if (cookies.isEmpty()) {
             System.out.println("None");
         } else {
             for (int i = 0; i < cookies.size(); i++) {
                 System.out.println("- " + cookies.get(i).toString());
             }
         }
         
         get2.releaseConnection();
         //HttpGet httpget1 = new HttpGet("http://localhost:8080/scape/lifecycle/scape:86473654-b593-480f-9be8-8fabf2b54132");
        
		
		HttpGet get1 = new HttpGet("http://localhost:8080/scape/entity/scape:86473654-b593-480f-9be8-8fabf2b54132");
        HttpResponse response1 = httpclient.execute(get1);
        HttpEntity entity1 = response1.getEntity();
        System.out.println("Load Examples HTTP Status Code: " + IOUtils.toString(entity1.getContent()));
        EntityUtils.consume(entity1);
	             
        System.out.println("Load Examples HTTP Status Code: " + response1.getStatusLine().getStatusCode());
	
        get1.releaseConnection();        
         
	             
        
 		
	}

}
