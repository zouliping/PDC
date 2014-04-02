package db;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import utils.DatabaseConfig;

public class MyDBManager {

	private final static String driver = "org.postgresql.Driver";
	private String url;
	private static String user;
	private static String pwd;

	private Connection con;
	private Statement stmt;
	private PreparedStatement ps;

	public MyDBManager() {
		try {
			DatabaseConfig.whichPlatform = 0;
			DatabaseConfig.set();
			url = DatabaseConfig.url;
			user = DatabaseConfig.user;
			pwd = DatabaseConfig.pwd;

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
	 * insert a data into user or admin
	 * 
	 * @param tableName
	 */
	public void insertIntoTable(String tableName, String idName, String id,
			String pwd, String token) {
		String sql = "INSERT INTO " + tableName + "(" + idName
				+ ",pwd,token) values (?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, id);
			ps.setString(2, pwd);
			ps.setString(3, token);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * insert a data into service table
	 * 
	 * @param name
	 * @param packagename
	 * @param appkey
	 */
	public void insertIntoServiceTable(String name, String packagename) {
		String sql = "INSERT INTO service (name, packagename) values (?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, name);
			ps.setString(2, packagename);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * insert into table rules
	 * 
	 * @param uid
	 * @param classname
	 * @param allpro
	 * @param pro
	 * @param level
	 */
	public void insertIntoRules(String uid, String classname, Boolean allpro,
			String pro[], Integer level) {
		String sql = "INSERT INTO rules (uid,classname,allpro,pro,level) values (?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, uid);
			ps.setString(2, classname);
			ps.setBoolean(3, allpro);
			Array array = con.createArrayOf("varchar", pro);
			ps.setArray(4, array);
			ps.setInt(5, level);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * query
	 * 
	 * @param sql
	 * @return
	 */
	public Boolean query(String sql) {
		System.out.println(sql);
		try {
			Statement statement = con.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(sql);
			if (rs.first()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * confirm user info
	 * 
	 * @param token
	 * @return
	 */
	public Boolean confirmUser(String token) {
		String sql = "SELECT * FROM users WHERE token = \'" + token + "\'";
		try {
			Statement statement = con.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery(sql);
			if (rs.first()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Boolean update(Boolean allpro, String pro[], Integer level,
			String classname, String uid) {
		String sql = "UPDATE rules SET allpro = ?, level = ?, pro = ? where classname = \'"
				+ classname + "\' and uid = \'" + uid + "\'";
		try {
			ps = con.prepareStatement(sql);
			ps.setBoolean(1, allpro);
			ps.setInt(2, level);
			Array array = con.createArrayOf("varchar", pro);
			ps.setArray(3, array);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * get list
	 * 
	 * @param sql
	 * @return
	 */
	public ArrayList<String> getList(String sql) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String f = rs.getString(1);
				list.add(f);
			}
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void testQuery(String sql) {
		try {
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Array array = rs.getArray(1);
				String[] strArray = (String[]) array.getArray();
				System.out.println(array.toString() + strArray[0]);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

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
