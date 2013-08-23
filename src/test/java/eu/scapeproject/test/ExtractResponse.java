package eu.scapeproject.test;

import org.junit.Test;


/**
 * Test class to extract the response of the TCK
 * @author mhn
 *
 */
public class ExtractResponse {
	
	
	public static String response = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" 
                                      + "<scape:identifier xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:scape=\"http://scapeproject.eu/model\""
                                      + " xmlns:ns3=\"http::/scapeproject.eu/model\" xmlns:xlink=\"http://www.w3.org/1999/xlink\""
                                      + " xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:premis=\"http://www.loc.gov/standards/premis\""
                                      + " xmlns:textmd=\"info:lc/xmlns/textmd-v3\" xmlns:mix=\"http://www.loc.gov/mix/v10\"" 
                                      + " xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\"" 
                                      + " xmlns:amd=\"http://www.loc.gov/AMD/\" xmlns:vmd=\"http://www.loc.gov/videoMD/\"" 
                                      + " xmlns:ns13=\"info:lc/xmlns/premis-v2\" type=\"String\">"
                                      + "<scape:value>IE-5ed56c80-3b7b-40bd-85d6-695f5cf894de</scape:value>"
                                      +	"</scape:identifier>";		
	
	
	@Test
	public void getSipID() { 
		
		// Du kannst entweder mit XPath the value extrahieren oder einfach mit String methoden ausschneiden. 
		String begin = "<scape:value>";
		String end = "</scape:value>";
		int beginIndex = response.indexOf(begin);
		int endIndex = response.indexOf(end);
		
		String value = response.substring(beginIndex+begin.length(), endIndex);
		System.out.println(value);

		
		
	}

}
