package eu.scapeproject.pt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.SerializationException;

import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;

/**
 * Helper class to handle file io, generate random sips and other stuff
 * @author mhn
 *
 */
public class LoaderIO {
	
	public String[] getFiles(String sipdir) { 
		File dir = new File(sipdir);
		String[] fileList = dir.list(new FilenameFilter() {
		    public boolean accept(File d, String name) {
		       return name.endsWith(".xml");
		    }
		});
		
		return fileList;
	}
	
	public  void generateSips(int numberOfSips) throws FileNotFoundException, SerializationException, JAXBException { 
		for (int i=0; i<numberOfSips; i++) {
			String sipFileName = "sips/mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=TestUtil.createRandomEntity();
			FileOutputStream out=new FileOutputStream(xmlFile);
			SCAPEMarshaller.getInstance().serialize(entity, out);
		}
	}

}
