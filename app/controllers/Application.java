package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		ObjectNode result = Json.newObject();
		result.put("status", "OK");
		result.put("message", "Hello zlp");
		return ok(result);
	}

}
