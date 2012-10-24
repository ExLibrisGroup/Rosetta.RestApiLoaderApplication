package eu.scapeproject;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Deque;
import java.util.LinkedList;

import eu.scapeproject.Sip.STATE;

public class LoaderDao {
	private Connection conn;
	private PreparedStatement insertStatment;
	private PreparedStatement updateStatment;
	private PreparedStatement getSipsStatment;
	private PreparedStatement getNextSeq;
	private PreparedStatement deleteStatment;

    public LoaderDao() throws Exception {
    	String dbFile = "scape.db";
        Class.forName("org.hsqldb.jdbcDriver");
        conn = DriverManager.getConnection("jdbc:hsqldb:" + dbFile, "sa", "");

        if (!isSipTableExist()) {
        	System.out.println("Creating Data Model...");
        	try {
        		createSipSequence();
				createSipTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

        createStatments();
    }

    public Sip insertSip(Sip sip) throws SQLException {
    	insertStatment.setLong(1, getSipIdNextVal());
    	insertStatment.setString(2, sip.getSipId());
    	insertStatment.setString(3, sip.getUri().toASCIIString());
    	insertStatment.setString(4, sip.getState().toString());
    	insertStatment.setString(5, sip.getDescription());
    	insertStatment.executeUpdate();

    	return sip;
    }

    public Sip updateSip(Sip sip) throws SQLException {
    	updateStatment.setString(1, sip.getSipId());
    	updateStatment.setString(2, sip.getUri().toASCIIString());
    	updateStatment.setString(3, sip.getState().toString());
    	updateStatment.setString(4, sip.getDescription());
    	updateStatment.setLong(5, sip.getId());
    	updateStatment.executeUpdate();

    	return sip;
    }

    public Deque<Sip> getAllSipsByState(STATE state) throws SQLException {
    	Deque<Sip> sips = new LinkedList<Sip>();

    	getSipsStatment.setString(1, state.toString());
    	ResultSet rs = getSipsStatment.executeQuery();

    	Sip sip = null;
    	while (rs.next()) {
    		sip = new Sip();
    		sip.setId(rs.getInt(1));
    		sip.setSipId(rs.getString(2));
    		sip.setUri(URI.create(rs.getString(3)));
    		sip.setState(STATE.valueOf(rs.getString(4)));
    		sip.setDescription(rs.getString(5));
    		sips.add(sip);
    	}

    	return sips;
    }

    public void deleteSacpeSips() throws SQLException {
    	deleteStatment.executeUpdate();
    }

    public void closeStatments() throws SQLException {
    	insertStatment.close();
    	updateStatment.close();
    	getSipsStatment.close();
    	deleteStatment.close();
    	getNextSeq.close();
    }

    private void createStatments() throws SQLException {
    	insertStatment = conn.prepareStatement("INSERT INTO SCAPE_SIP (ID, SIP_ID, URI, STATE, DESCRIPTION) VALUES (?, ?, ?, ?, ?)");
    	updateStatment = conn.prepareStatement("UPDATE SCAPE_SIP SET SIP_ID=?, URI=?, STATE=?, DESCRIPTION=? WHERE ID=?");
    	getSipsStatment = conn.prepareStatement("SELECT ID, SIP_ID, URI, STATE, DESCRIPTION FROM SCAPE_SIP WHERE STATE=?");
    	deleteStatment = conn.prepareStatement("DELETE FROM SCAPE_SIP");
    	getNextSeq = conn.prepareStatement("CALL NEXT VALUE FOR SIP_ID");
    }

    private long getSipIdNextVal() throws SQLException {
    	ResultSet rs = null;
    	rs = getNextSeq.executeQuery();
    	rs.next();
    	long ret = rs.getLong(1);
    	rs.close();
    	return ret;
    }

    private void createSipTable() throws SQLException {
    	String sql =
    		"CREATE TABLE SCAPE_SIP "+
    	    "(ID BIGINT," +
    	    "SIP_ID varchar(255),"+
    	    "URI varchar(255),"+
    	    "STATE varchar(255),"+
    	    "DESCRIPTION varchar(255))";

        Statement st = conn.createStatement();
        int i = st.executeUpdate(sql);
        if (i == -1) {
           throw new RuntimeException("Unable to create table 'SCAPE_SIP'");
        }

        st.close();
    }

    private void createSipSequence() throws SQLException {
    	String sql = "CREATE SEQUENCE SIP_ID;";

        Statement st = conn.createStatement();
        int i = st.executeUpdate(sql);
        if (i == -1) {
           throw new RuntimeException("Unable to create table 'sip_id' sequence");
        }

        st.close();
    }

    private boolean isSipTableExist() throws SQLException {
    	DatabaseMetaData md = conn.getMetaData();
    	ResultSet rs = md.getTables(null, null ,"SCAPE_SIP", new String[] {"TABLE"});
    	boolean ret = false;

    	if (rs.next()) {
    		ret = true;
    	}

    	rs.close();
    	return ret;
    }

    public void shutdown() throws SQLException {
    	Statement st = conn.createStatement();
    	st.execute("SHUTDOWN");
    	insertStatment.close();
    	conn.close();
    }
}
