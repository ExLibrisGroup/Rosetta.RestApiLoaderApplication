package eu.scapeproject.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.junit.Test;

public class HDFSIO {

	@Test
	/**
	 * Reads from hdfs - needs to have the correct hadoop-core version in POM!!!
	 * 
	 * @throws IOException
	 */
	public void test() throws IOException {
		String pathToFile="/user/mhn/abometsseqfilemini.seq";
		Path seqFilePath = new Path(pathToFile);
        Configuration conf = new Configuration();
        String hadoop_home = System.getenv("HADOOP_HOME");
        conf.addResource(new Path(hadoop_home+"/conf/core-site.xml"));
        conf.addResource(new Path(hadoop_home+"/conf/hdfs-site.xml"));
        FileSystem fs = FileSystem.get(conf);
        SequenceFile.Reader reader = new SequenceFile.Reader(fs, seqFilePath , conf);
        Text key = new Text();
        BytesWritable val = new BytesWritable();
        
        while (reader.next(key, val)) {
          int index = key.toString().lastIndexOf("/");
          String filename = key.toString().substring(index+1);
          System.out.println(filename);
        
        }
	}

}
