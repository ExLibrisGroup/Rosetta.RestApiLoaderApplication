package eu.scapeproject.pt.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
		Map configuration = this.checkFS(pathToFile);
        SequenceFile.Reader reader = new SequenceFile.Reader((FileSystem)configuration.get("fs"), seqFilePath , (Configuration)configuration.get("conf"));
        Text key = new Text();
        BytesWritable val = new BytesWritable();
        logger.info("Extract Sequence File:"); 
        while (reader.next(key, val)) {
          int index = key.toString().lastIndexOf("/");
          String filename = key.toString().substring(index+1);
          byte[] _mets = val.getBytes();
          int bytesLen = val.getLength();
          byte[] slicedBytes = new byte[bytesLen];
          System.arraycopy(_mets, 0, slicedBytes, 0, bytesLen);
          String mets1 = new String(slicedBytes, "UTF-8");
//          int endIndex = mets.indexOf("</METS:mets>");
//          String mets1 = mets.substring(0, endIndex+12);
          File file = new File("sips/" + filename);
          logger.info(file.getPath());
          FileUtils.writeStringToFile(file,mets1,"UTF-8");
        }
        logger.info("Finish extract Sequence file");
      }
	
	/**
	 * extract the XML files out of a zipped folder
	 * @param patToFile
	 * @throws IOException 
	 */
	public void extractZipFile(String pathToFile) throws IOException {
		
		ZipInputStream zis =
	              new ZipInputStream(new FileInputStream(pathToFile));
	      
	    byte[] buffer = new byte[4096];
	    ZipEntry ze;
	    while ((ze = zis.getNextEntry()) != null) {
	       System.out.println("Extracting: "+ze);
	       FileOutputStream fos = new FileOutputStream("sips/"+ze.getName());
	       int numBytes;
	       while ((numBytes = zis.read(buffer, 0, buffer.length)) != -1) {
	           fos.write(buffer, 0, numBytes);
	       }
	       zis.closeEntry();
	    }
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
	
	private Map checkFS(String pathtofile) throws IOException { 
		
		File file = new File(pathtofile); 
		Configuration conf = new Configuration();
		
		// If file does not exists on local file system check if it lives on HDFS
		String hadoop_home = ""; 
		if (!file.exists()) { 
			hadoop_home = System.getenv("HADOOP_HOME");
			if (hadoop_home != null && hadoop_home.length() > 0 ) { 
				conf.addResource(new Path(hadoop_home+"/conf/core-site.xml"));
				conf.addResource(new Path(hadoop_home+"/conf/hdfs-site.xml"));
			} else { 
				System.out.println("ERROR: HADOOP_HOME not set!");
				System.exit(0);
			}
			
		}
		
		FileSystem fs = FileSystem.get(conf);
		Map map = new HashMap();
		map.put("conf", conf);
		map.put("fs", fs);
        return map;
		
	}

}
