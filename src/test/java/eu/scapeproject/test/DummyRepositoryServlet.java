package eu.scapeproject.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;



public class DummyRepositoryServlet extends GenericServlet {

	private static final long serialVersionUID = 1L;

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        try {
			IntellectualEntity ie =
					SCAPEMarshaller.getInstance().deserialize(IntellectualEntity.class, request.getInputStream());
			System.out.println("Repository Received Ingest Request for IE ID:'" + ie.getIdentifier().getValue() + "' ");

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

        int sipId = Math.abs(new Random().nextInt());
        System.out.println("Repository Generated Sip ID:'" + sipId + "' ");
        out.print(sipId);
    }
}
