package utils;

public class StringUtil {
	public static String STRING_NULL = "null";
	public static String CLASSNAME_PREFIX = "http://www.w3.org/";
	public static String START_TIME = "START OF";
	public static String END_TIME = "  END OF";
	public static String NEW_DATA = "new_";
	public static String UPDATE_DATA = "update_";

	public static void printStart(String title) {
		System.out.println("******************** START OF " + title
				+ " ********************");
	}

	public static void printEnd(String title) {
		System.out.println("******************** END OF " + title
				+ " **********************");
	}

	/**
	 * remove ^^
	 * 
	 * @param oldStr
	 * @return
	 */
	public static String removeSpecialChar(String oldStr) {
		if (oldStr.contains("^^")) {
			oldStr = oldStr.substring(0, oldStr.indexOf("^^"));
		}
		return oldStr;
	}

	// PRINT STR
	public static String HELLO = "hello pdc";
	public static String LOGIN = "login";
	public static String REGISTER_USER = "register user";
	public static String REGISTER_APP = "register service";
	public static String SET_PRIVACY_RULE = "set privacy rules";
	public static String SET_DATA_CHANGE_RULE = "set data change rule";
	public static String GET_DATA_CHANGE_RULE = "get data change rule";
	public static String GET_OWL_FILE = "get OWL file";
	public static String GET_ALL_CHASSES = "get all classes";
	public static String GET_CLASS_PROPERTIES = "get a class' properties";
	public static String GET_CLASS_RELATION = "get a relation (ontology)";
	public static String GET_LABEL = "get labels (ontology)";
	public static String ADD_LABEL = "add labels (ontology)";
	public static String GET_RELATED_CLASSES = "get related classes";
	public static String ADD_CLASS = "add a class (ontology)";
	public static String ADD_CLASS_RELATION = "add a relation (ontology)";
	public static String GET_INDIVIDUALS = "get a class'a individuals";
	public static String GET_INDIVIDUAL_PROPERTY = "get a individual's properties";
	public static String GET_BY_LABEL = "get individuals by labels";
	public static String UPDATE_INDIVIDUAL = "update individual";
	public static String ADD_INDIVIDUAL_RELATION = "add relation (individual)";
	public static String REMOVE_INDIVIDUAL = "remove a individual";
	public static String REMOVE_INDIVIDUAL_RELATION = "remove relation (individual)";

}
