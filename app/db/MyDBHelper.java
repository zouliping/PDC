package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import utils.UserUtil;

import com.hp.hpl.jena.db.DBConnection;

public class MyDBHelper {

	private final static String driver = "com.mysql.jdbc.Driver";
	private static String url;
	private final static String db = "MySQL";
	private final static String user = "root";
	private final static String pwd = "123456";

	private DBConnection con;

	public MyDBHelper() {
		url = "jdbc:mysql://localhost/" + UserUtil.uid
				+ "?useUnicode=true&characterEncoding=utf8";
	}

	public void createDB() {
		try {
			String mUrl = "jdbc:mysql://localhost/testjena?useUnicode=true&characterEncoding=utf8";
			Connection connection = DriverManager
					.getConnection(mUrl, user, pwd);
			Statement stmt = connection.createStatement();

			// remember to set database character
			stmt.executeUpdate("create database if not exists " + UserUtil.uid
					+ " default character set utf8 collate utf8_bin");

			stmt.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
