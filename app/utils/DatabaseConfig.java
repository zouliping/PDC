package utils;

import java.net.URI;
import java.net.URISyntaxException;

public class DatabaseConfig {

	public static Boolean isLocal = true;
	public static String url;
	public static String user;
	public static String pwd;

	public static void set() {
		if (isLocal) {
			url = "jdbc:postgresql://localhost/pdc?useUnicode=true&characterEncoding=utf8";
			user = "zouliping";
			pwd = "postgres";
		} else {
			URI dbUri;
			try {
				dbUri = new URI(System.getenv("DATABASE_URL"));
				user = dbUri.getUserInfo().split(":")[0];
				pwd = dbUri.getUserInfo().split(":")[1];
				url = "jdbc:postgresql://" + dbUri.getHost() + ':'
						+ dbUri.getPort() + dbUri.getPath();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
	}
}
