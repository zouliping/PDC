package utils;

import java.util.ArrayList;

import play.Logger;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonUtil {

	/**
	 * get true json
	 * 
	 * @return
	 */
	public static ObjectNode getTrueJson(String title) {
		ObjectNode result = Json.newObject();
		result.put("result", true);
		Logger.info(result.toString());
		StringUtil.printEnd(title);
		return result;
	}

	/**
	 * get false json
	 * 
	 * @return
	 */
	public static ObjectNode getFalseJson(Integer error, String title) {
		ObjectNode result = Json.newObject();
		result.put("result", error);
		Logger.info(result.toString());
		StringUtil.printEnd(title);
		return result;
	}

	/**
	 * add a list as json value
	 * 
	 * @param key
	 * @param strList
	 * @return
	 */
	public static ObjectNode addList2Json(String key,
			ArrayList<String> strList, String title) {
		ObjectNode result = Json.newObject();
		ArrayNode an = result.arrayNode();

		for (String tmp : strList) {
			an.add(tmp);
		}

		result.putArray(key).addAll(an);
		Logger.info(result.toString());
		StringUtil.printEnd(title);
		return result;
	}

}
