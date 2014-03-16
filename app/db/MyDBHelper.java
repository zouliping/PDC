package db;

import java.sql.SQLException;

import utils.DatabaseConfig;

import com.hp.hpl.jena.db.DBConnection;

public class MyDBHelper {

	private final static String driver = "org.postgresql.Driver";

	private final static String db = "PostgreSQL";
	private static String url;
	private static String user;
	private static String pwd;

	private DBConnection con;

	public MyDBHelper() {
		DatabaseConfig.whichPlatform = 0;
		DatabaseConfig.set();
		url = DatabaseConfig.url;
		user = DatabaseConfig.user;
		pwd = DatabaseConfig.pwd;
	}

	/**
	 * get db connection
	 * 
	 * @param dbUrl
	 * @param dbUser
	 * @param dbPwd
	 * @param dbName
	 * @return
	 */
	public DBConnection getConnection() {
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		con = new DBConnection(url, user, pwd, db);
		return con;
	}

	/**
	 * close the connection
	 */
	public void closeConnection() {
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
