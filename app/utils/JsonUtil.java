package utils;

import java.util.ArrayList;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtil {

	/**
	 * get true json
	 * 
	 * @return
	 */
	public static ObjectNode getTrueJson() {
		ObjectNode result = Json.newObject();
		result.put("result", true);
		return result;
	}

	/**
	 * get false json
	 * 
	 * @return
	 */
	public static ObjectNode getFalseJson() {
		ObjectNode result = Json.newObject();
		result.put("result", false);
		return result;
	}

	/**
	 * add a list as json value
	 * 
	 * @param key
	 * @param strList
	 * @return
	 */
	public static ObjectNode addList2Json(String key, ArrayList<String> strList) {
		ObjectNode result = Json.newObject();
		ArrayNode an = result.arrayNode();

		for (String tmp : strList) {
			an.add(tmp);
		}

		result.putArray(key).addAll(an);
		System.out.println(result);
		return result;
	}

}
