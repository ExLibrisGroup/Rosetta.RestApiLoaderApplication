package eu.scapeproject.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.util.ScapeMarshaller;



public class EntitySyncServlet extends GenericServlet {

	private static final long serialVersionUID = 1L;

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
			IntellectualEntity ie =
					ScapeMarshaller.newInstance().deserialize(IntellectualEntity.class, request.getInputStream());
			System.out.println("Repository Received Ingest Request for IE ID:'" + ie.getIdentifier().getValue() + "' ");

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

        int sipId = Math.abs(new Random().nextInt());
        System.out.println("Repository Generated Sip ID:'" + sipId + "' ");
        
        String output = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<scape:identifier xmlns:mets=\"http://www.loc.gov/METS/\"" + 
                        "xmlns:scape=\"http://scapeproject.eu/model\" xmlns:ns3=\"http::/scapeproject.eu/model\"" +
                        "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
                        "xmlns:premis=\"http://www.loc.gov/standards/premis\" xmlns:textmd=\"info:lc/xmlns/textmd-v3\""+
                        "xmlns:mix=\"http://www.loc.gov/mix/v10\" xmlns:fits=\"http://hul.harvard.edu/ois/xml/ns/fits/fits_output\""+
                        "xmlns:amd=\"http://www.loc.gov/AMD/\" xmlns:vmd=\"http://www.loc.gov/videoMD/\" xmlns:ns13=\"info:lc/xmlns/premis-v2\" type=\"String\">" +
                        "<scape:value>" +sipId + "</scape:value>" +
                        "</scape:identifier>";
        
        out.print(output);
    }
}
