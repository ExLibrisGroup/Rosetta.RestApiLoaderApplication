package eu.scapeproject.test;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import eu.scapeproject.pt.main.Loader;

public class AppLoaderEsciDoc {
	
	
	@Test
	public void load() { 
		
		String[] args = {"-d /tmp/escidoc", "-u http://localhost:8080", "-i scape/entity", "-l scape/lifecycle", "-m true"};
		try {
			Loader.main(args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
