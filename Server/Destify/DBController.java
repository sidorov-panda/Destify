package Destify;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class DBController {
	private static final DBController dbcontroller = new DBController();
	private static Connection connection;
	private static final String DB_PATH = defaultDirectory() +"/Destify/protocol.db";

	private static String defaultDirectory() {
	    String OS = System.getProperty("os.name").toUpperCase();
	    if (OS.contains("WIN")) return System.getenv("APPDATA");
	    else if (OS.contains("MAC"))  return System.getProperty("user.home") + "/Library/Application Support";
	    else if (OS.contains("NUX"))  return System.getProperty("user.home");
	    return System.getProperty("user.dir");
	}
	
	public static String getLastBitFromUrl(final String url){
	    return url.replaceFirst(".*/([^/?]+).*", "$1");
	}

	public static DBController getInstance() {
		return dbcontroller;
	}

	void initDBConnection() {
		try {
			if (connection != null) return;
			
			File theDir = new File(DB_PATH.replace(getLastBitFromUrl(DB_PATH), ""));
			if (!theDir.exists()) { theDir.mkdir(); }

			connection = DriverManager.getConnection("jdbc:sqlite:" +DB_PATH);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void logData(String title, String subtitle, String message, String time) {
		try {			
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE TABLE IF NOT EXISTS protocol (title, subtitle, message, time);");

			PreparedStatement ps = connection.prepareStatement("INSERT INTO protocol VALUES (?, ?, ?, ?);");

			ps.setString(1, title);
			ps.setString(2, subtitle);
			ps.setString(3, message);
			ps.setString(4, time);
			ps.addBatch();

			connection.setAutoCommit(false);
			ps.executeBatch();
			connection.setAutoCommit(true);

		} catch (SQLException e) { }
	}
	
	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) { }
	}

	public List<String[]> getProtocol() {
		try {			
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM protocol;");

			List<String[]> collection = new ArrayList<String[]>();
			
			while (rs.next()) {
				String[] parts = new String[4];
				
				String subtitle = new String(rs.getString("subtitle").replace("<br/>", ""));
				
				parts[0] = rs.getString("title");
				parts[1] = subtitle.equals("") ? "NO SUBTITLE":subtitle;
				parts[2] = rs.getString("time");
				parts[3] = rs.getString("message");
				
				collection.add(parts);
				
				parts = null;
			}
			
			rs.close();
			
			return collection;
		} catch (SQLException e) { }
		
		return null;
	}
}