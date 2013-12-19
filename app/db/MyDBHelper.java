package db;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import com.hp.hpl.jena.db.DBConnection;

public class MyDBHelper {

	private final static String driver = "org.postgresql.Driver";
	// private static String url =
	// "jdbc:postgresql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
	// private final static String db = "MySQL";
	// private final static String user = "root";
	// private final static String pwd = "123456";

	private final static String db = "PostgreSQL";
	private static String url;
	private static String user;
	private static String pwd;

	private final static String db_url = "postgres://ffvtcujmlleplt:hlmxYInnbmNIR2c6rW7TH3ldS3@ec2-54-225-101-18.compute-1.amazonaws.com:5432/d5p1jrg291roqq";

	private DBConnection con;

	public MyDBHelper() {
		try {
			URI dbUri = new URI(System.getenv(db_url));
			user = dbUri.getUserInfo().split(":")[0];
			pwd = dbUri.getUserInfo().split(":")[1];
			url = "jdbc:postgresql://" + dbUri.getHost() + ':'
					+ dbUri.getPort() + dbUri.getPath();
		} catch (URISyntaxException e) {
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
