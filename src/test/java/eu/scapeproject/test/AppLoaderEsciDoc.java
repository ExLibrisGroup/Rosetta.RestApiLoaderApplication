package eu.scapeproject.test;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import eu.scapeproject.pt.auth.EsciDocAuthentication;
import eu.scapeproject.pt.main.Loader;

public class AppLoaderEsciDoc {
	
	
	@Test
	public void load() { 
		
		String[] args = {"-d /tmp/escidoc", "-r http://localhost:8080", "-i scape/entity-async", "-l scape/lifecycle", "-t 3", "-u sysadmin", "-p sysadmin"};
		try {
			Loader.main(args);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
