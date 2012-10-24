package eu.scapeproject.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.io.IOUtils;



public class EntityLifecycleServlet extends GenericServlet {

	private static final long serialVersionUID = 1L;

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String entityId = request.getParameter("Id");

        String lifeCycleXml = "<lifecyclestate id=\"" + entityId + "\" state=\"INGESTED\">" +
    	                      	"<details>Ingest finished successfully at 6/27/2012 13:34:57</details>" +
    	                      "</lifecyclestate>";

        out.print(lifeCycleXml);
    }
}
