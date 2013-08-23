package eu.scapeproject.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


/**
 * extract the lifecycle state out of the XML response
 * @author mhn
 *
 */
public class ExtractLifecycleResponse {
	
	private static final String response_old ="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
											"<lifecycleState xmlns:mets=\"http://www.loc.gov/METS/\" " +
											"xmlns:scape=\"http://scapeproject.eu/model\"  " +
											"xmlns:ns3=\"http::/scapeproject.eu/model\" " +
											"xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " +
											"xmlns:premis=\"http://www.loc.gov/standards/premis\" xmlns:textmd=\"info:lc/xmlns/textmd-v3\" " +
											"xmlns:mix=\"http://www.loc.gov/mix/v10\" xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\" " + 
											"xmlns:amd=\"http://www.loc.gov/AMD/\" xmlns:vmd=\"http://www.loc.gov/videoMD/\" " + 
											"xmlns:ns13=\"info:lc/xmlns/premis-v2\" lifecyclestate=\"INGESTING\"/>"; 
	
	
	private static final String response ="<?xml version=¸\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
											"<lifecycleState xmlns:mets=\"http://www.loc.gov/METS/\" " + 
											"xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:scape=\"http://scapeproject.eu/model\" " + 
											"xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\" " +
											"xmlns:textmd=\"info:lc/xmlns/textmd-v3\" xmlns:ns7=\"http://www.loc.gov/audioMD/\" " + 
											"xmlns:marc=\"http://www.loc.gov/MARC21/slim\" xmlns:ns9=\"http://www.loc.gov/mix/v20\" " +
											"xmlns:vmd=\"http://www.loc.gov/videoMD/\" " +
											"xmlns:premis=\"info:lc/xmlns/premis-v2\" " + 
											"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" " + 
											"lifecyclestate=\"INGESTED\"></lifecycleState>";
	
	
	@Test
	public void testResponse() { 
		
		//Pattern pattern = Pattern.compile("lifecyclestate=.*\"/>"); 
		Pattern pattern = Pattern.compile("lifecyclestate=.*\">"); 
		Matcher matcher = pattern.matcher(response);
		String result = "not defined"; 
		while (matcher.find()) { 
		     String[] x = matcher.group().split("=");
		     if(x.length > 1) {
		       //result = x[1].substring(1, x[1].length()-3);
		       result = x[1].substring(1, x[1].length()-2);
		     } 
		}
		
		System.out.println(result);
		
	}
	

}
