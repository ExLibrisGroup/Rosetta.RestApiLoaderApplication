package eu.scapeproject.pt.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import org.apache.hadoop.conf.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import eu.scapeproject.model.IntellectualEntity;
import eu.scapeproject.model.mets.SCAPEMarshaller;
import eu.scapeproject.model.util.TestUtil;

/**
 * Helper class to handle file io, generate random sips and other stuff
 * @author mhn
 *
 */
public class LoaderIO {
	
	private static Logger logger =  Logger.getLogger( LoaderIO.class.getName());
	
	/**
	 * reads all xml uri files in a directory
	 * @param sipdir
	 * @return fileList
	 */
	public String[] getFiles(String sipdir) { 
		File dir = new File(sipdir);
		String[] fileList = dir.list(new FilenameFilter() {
		    public boolean accept(File d, String name) {
		       return name.endsWith(".xml");
		    }
		});
		return fileList;
	}
	
	/**
	 * extracts a given Hadoop sequence file into a local temporary sips directory. 
	 * @param pathToFile
	 * @param sipdir
	 * @throws IOException
	 */
	public void extractSeqFile(String pathToFile) throws IOException {
		
		Path seqFilePath = new Path(pathToFile);
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath , conf);
        Text key = new Text();
        BytesWritable val = new BytesWritable();
        logger.info("Extract Sequence File:"); 
        while (reader.next(key, val)) {
          int index = key.toString().lastIndexOf("/");
          String filename = key.toString().substring(index+1);
          byte[] _mets = val.getBytes();
          String mets = new String(_mets, "UTF-8");
          String mets1 = mets.replaceAll("(?i)<METS", "<mets").replaceAll("(?i)</METS", "</mets");
          File file = new File("sips/" + filename);
          logger.info(file.getPath());
          FileUtils.writeStringToFile(file,mets1,"UTF-8");
        }
        logger.info("Finish extract Sequence file");
      }
	
	
	
	/**
	 * Generate example mets files in directory, used for testing purpose
	 * @param numberOfSips
	 * @throws FileNotFoundException
	 * @throws SerializationException
	 * @throws JAXBException
	 */
	public  void generateSips(int numberOfSips) throws FileNotFoundException, SerializationException, JAXBException { 
		for (int i=0; i<numberOfSips; i++) {
			String sipFileName = "sips/mets_entity_" + i + ".xml";
			java.io.File xmlFile=new java.io.File(sipFileName);
			IntellectualEntity entity=TestUtil.createRandomEntity();
			FileOutputStream out=new FileOutputStream(xmlFile);
			SCAPEMarshaller.getInstance().serialize(entity, out);
		}
	}
	
	/**
	 * cleans the local sips directory
	 * @throws IOException 
	 */
	public void cleandir() throws IOException { 
		File sips = new File("sips/");
		sips.mkdir();
		FileUtils.cleanDirectory(sips);
	}

}
