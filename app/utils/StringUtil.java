package utils;

public class StringUtil {
	public static String STRING_NULL = "null";
	public static String CLASSNAME_PREFIX = "http://www.w3.org/";
	public static String START_TIME = "START OF";
	public static String END_TIME = "END OF";

	public static void printStr(String title, String time) {
		System.out.println("******************** " + time + " " + title
				+ "******************** ");
	}
}
