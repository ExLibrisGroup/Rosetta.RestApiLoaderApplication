package eu.scapeproject.pt.auth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public final class EsciDocAuthentication implements IAuthentication {
	
	private static final DefaultHttpClient httpclient = new DefaultHttpClient();

	
	public EsciDocAuthentication() { 
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy());
	}
	
	
	@Override
	public DefaultHttpClient logon() {
		HttpGet httpget = new HttpGet("http://localhost:8080/aa/login/login.html");
        HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
		    EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 HttpPost httppost = new HttpPost("http://localhost:8080/aa/j_spring_security_check");
         httppost.setHeader("Referer", "http://localhost:8080/aa/login/login.html");
         httppost.setHeader("Connection", "keep-alive");
         httppost.getParams().setParameter("http.protocol.handle-redirects",true);
             
         List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("j_username", "sysadmin"));
         nvps.add(new BasicNameValuePair("j_password", "esciDoc"));
         nvps.add(new BasicNameValuePair("Abschicken", "submit"));

         try {
			httppost.setEntity(new UrlEncodedFormEntity(nvps, "iso-8859-1"));
			HttpHost target = new HttpHost("localhost",8080); 
	        HttpResponse postresponse = httpclient.execute(target,httppost);
	        HttpEntity entity2 = postresponse.getEntity();
	        EntityUtils.consume(entity2);
	        httppost.releaseConnection();
	        
	        httppost.releaseConnection();    
	        
	        //needed to set ESCIDOC cookie
            HttpGet get2 = new HttpGet("http://localhost:8080/aa/login?target=http://localhost:8080/AdminTool/");
	        HttpResponse response2 = httpclient.execute(get2);   
	        get2.releaseConnection();
	        
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
       
       return httpclient;

	}

}
