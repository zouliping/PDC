package utils;

import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseConfig {

	/**
	 * 0 means local; 1 means heroku; 2 means openshift
	 */
	public static int whichPlatform = 0;
	public static String url = "jdbc:postgresql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
	public static String user = "zouliping";
	public static String pwd = "postgres";

	public static void set() {
		if (whichPlatform == 0) {
			url = "jdbc:postgresql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
			user = "zouliping";
			pwd = "postgres";
		} else if (whichPlatform == 1) {
			URI dbUri;
			try {
				dbUri = new URI(System.getenv("DATABASE_URL"));
				user = dbUri.getUserInfo().split(":")[0];
				pwd = dbUri.getUserInfo().split(":")[1];
				url = "jdbc:postgresql://" + dbUri.getHost() + ':'
						+ dbUri.getPort() + dbUri.getPath()
						+ "?useUnicode=true&characterEncoding=utf8";
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		} else if (whichPlatform == 2) {
			url = "postgresql://$OPENSHIFT_POSTGRESQL_DB_HOST:$OPENSHIFT_POSTGRESQL_DB_PORT";
			user = "admina5w2kdk";
			pwd = "cZTXwvirbYbz";
		}
	}
}
