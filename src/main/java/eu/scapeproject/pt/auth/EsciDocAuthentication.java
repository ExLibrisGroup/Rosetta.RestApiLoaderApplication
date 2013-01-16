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

import eu.scapeproject.pt.main.Configuration;

public final class EsciDocAuthentication implements IAuthentication {
	
	private static final DefaultHttpClient httpclient = new DefaultHttpClient();
	private Configuration conf;
	
	public EsciDocAuthentication(Configuration conf) { 
		httpclient.setRedirectStrategy(new DefaultRedirectStrategy());
		this.conf = conf;
	}
	
	
	@Override
	public DefaultHttpClient logon() {
		HttpGet httpget = new HttpGet(conf.getUrl()+"/aa/login/login.html");
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
		
		 HttpPost httppost = new HttpPost(conf.getUrl()+"/aa/j_spring_security_check");
         httppost.setHeader("Referer", conf.getUrl()+"/aa/login/login.html");
         httppost.setHeader("Connection", "keep-alive");
         httppost.getParams().setParameter("http.protocol.handle-redirects",true);
             
         List <NameValuePair> nvps = new ArrayList <NameValuePair>();
         nvps.add(new BasicNameValuePair("j_username", conf.getUser()));
         nvps.add(new BasicNameValuePair("j_password", conf.getPassword()));
         nvps.add(new BasicNameValuePair("Abschicken", "submit"));

         try {
			httppost.setEntity(new UrlEncodedFormEntity(nvps, "iso-8859-1"));
			HttpHost target = new HttpHost(getDomain(conf.getUrl()),getPort(conf.getUrl())); 
	        HttpResponse postresponse = httpclient.execute(target,httppost);
	        HttpEntity entity2 = postresponse.getEntity();
	        EntityUtils.consume(entity2);
	        httppost.releaseConnection();
	        
	        httppost.releaseConnection();    
	        
	        //needed to set ESCIDOC cookie
            HttpGet get2 = new HttpGet(conf.getUrl()+"/aa/login?target="+conf.getUrl()+"/AdminTool/");
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
	
	
	private Integer getPort(String url) { 
		String[] _port = url.split(":");
		System.out.println(_port[2]);
		return Integer.parseInt(_port[2]);	
	}
	
	private String getDomain(String url) { 
		String[] _port = url.split(":");
		String domain = _port[1].substring(2);
		System.out.println(domain);
		return domain;	
	}

}
