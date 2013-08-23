package eu.scapeproject.test;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import eu.scapeproject.pt.main.Loader;

public class AppLoaderFedora4 {
	
		@Test
		public void load() { 
			
			// ONB Daten
			String input = "-d /home/mhn/git/loader-app/resources";
			
			// minimal SCAPE Daten
			//String input = "- d /tmp/testdaten/loader";
			
			String[] args = {input, "-r http://localhost:8080/fcrepo/rest", "-i scape/entity-async", "-l scape/lifecycle", "-t 1", "-u mhn","-p mhn"};
			try {
				Loader.main(args);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
}
