package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import utils.UserUtil;

public class MyDBManager {

	private final static String driver = "com.mysql.jdbc.Driver";
	private static String url = "jdbc:mysql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
	private final static String user = "root";
	private final static String pwd = "123456";
	private Connection con;
	private Statement stmt;
	private PreparedStatement ps;

	public MyDBManager() {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, pwd);
			stmt = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
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
