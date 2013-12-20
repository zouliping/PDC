package db;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import utils.UserUtil;

public class MyDBManager {

	// private final static String driver = "org.postgresql.Driver";
	// private static String url =
	// "jdbc:postgresql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
	// private final static String user = "zouliping";
	// private final static String pwd = "postgres";
	private String url;
	private static String user;
	private static String pwd;

	private Connection con;
	private Statement stmt;
	private PreparedStatement ps;

	public MyDBManager() {
		try {
			URI dbUri = new URI(System.getenv("DATABASE_URL"));
			user = dbUri.getUserInfo().split(":")[0];
			pwd = dbUri.getUserInfo().split(":")[1];
			url = "jdbc:postgresql://" + dbUri.getHost() + ':'
					+ dbUri.getPort() + dbUri.getPath();

			// Class.forName(driver);
			con = DriverManager.getConnection(url, user, pwd);
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * create the db for the user
	 */
	public void createDB() {
		try {
			// remember to set database character
			stmt.executeUpdate("create database if not exists " + UserUtil.uid
					+ " default character set utf8 collate utf8_bin");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * insert a data into user or admin
	 * 
	 * @param tableName
	 */
	public void insertIntoTable(String tableName, String idName, String id,
			String pwd) {
		String sql = "INSERT INTO " + tableName + "(" + idName
				+ ",pwd) values (?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.setString(2, pwd);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * query user or admin table to find uid and pwd
	 * 
	 * @param tableName
	 * @param id
	 * @param pwd
	 * @return
	 */
	public Boolean query(String tableName, String idName, String id, String pwd) {
		String sql = "SELECT * FROM " + tableName + " WHERE " + idName + "=\""
				+ id + "\" and pwd=\"" + pwd + "\"";
		System.out.println(sql);
		try {
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.first()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * get all users' id
	 * 
	 * @return
	 */
	public ArrayList<String> getAllUsers() {
		String sql = "SELECT uid FROM user";
		ArrayList<String> userList = new ArrayList<String>();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String user = rs.getString(1);
				userList.add(user);
			}
			return userList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * close the db
	 */
	public void closeDB() {
		try {
			stmt.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
