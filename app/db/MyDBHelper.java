package db;

import java.sql.SQLException;

import com.hp.hpl.jena.db.DBConnection;

public class MyDBHelper {

	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String url = "jdbc:mysql://localhost/testjena";
	private final static String db = "MySQL";
	private final static String user = "root";
	private final static String pwd = "123456";

	private DBConnection con;

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
