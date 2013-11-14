package controllers;

import java.util.ArrayList;
import java.util.Iterator;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JsonUtil;
import utils.ModelUtil;
import utils.MyOntModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class MyIndividual extends Controller {

	/**
	 * add a individual. client post a json, for example
	 * {"classname":"Writing","individualname"
	 * :"test","uid":"123456","booktitle":"titleTest"}
	 * 
	 * @return
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result update() {
		JsonNode json = request().body().asJson();
		System.out.println(json);

		String classname = json.findPath("classname").textValue();
		String individualname = json.findPath("individualname").textValue();
		String uid = json.findPath("uid").textValue();

		Long x = System.currentTimeMillis();

		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Long y = System.currentTimeMillis();
		System.out.println("create model " + (y - x));

		Long a = System.currentTimeMillis();
		// OntClass oc = ModelUtil.getExistedClass(classname);
		OntClass oc = model.getOntClass(prefix + classname);
		Long b = System.currentTimeMillis();
		System.out.println("a - b time " + (b - a));
		if (oc == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		Long c = System.currentTimeMillis();
		System.out.println("b - c time " + (c - b));

		Individual i;

		if (individualname == null || "".equals(individualname)) {
			i = oc.createIndividual(prefix + uid + "_" + classname + "_"
					+ System.currentTimeMillis());
			System.out.println("create");
		} else {
			// i = ModelUtil.getExistedIndividual(prefix + individualname);
			i = model.getIndividual(prefix + individualname);
		}
		Long d = System.currentTimeMillis();
		System.out.println("c - d time " + (d - c));

		Iterator<String> it;
		for (it = json.fieldNames(); it.hasNext();) {
			String pro = it.next();
			if ("classname".equals(pro) || "individualname".equals(pro)
					|| "uid".equals(pro)) {
				it.remove();
			}
		}
		// ArrayList<String> proList = ModelUtil.getPropertyList(oc);
		Long e = System.currentTimeMillis();
		System.out.println("d - e time " + (e - d));

		ModelUtil.addIndividualProperties(oc, i, it, json);
		Long f = System.currentTimeMillis();
		System.out.println("e -f time " + (f - e));

		return ok(JsonUtil.getTrueJson());
	}

	/**
	 * get a class's individuals
	 * 
	 * @param classname
	 * @return
	 */
	public static Result get(String classname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		ArrayList<String> indivList = new ArrayList<String>();

		if (oc == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		for (ExtendedIterator i = oc.listInstances(); i.hasNext();) {
			Individual individual = (Individual) i.next();
			indivList.add(individual.getLocalName());
		}

		return ok(JsonUtil.addList2Json(classname, indivList));
	}

	/**
	 * get a individual's properties
	 * 
	 * @param individualname
	 * @return
	 */
	public static Result getProperties(String individualname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual individual = model.getIndividual(prefix + individualname);

		if (individual == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		ObjectNode on = Json.newObject();
		for (StmtIterator si = individual.listProperties(); si.hasNext();) {
			StatementImpl sti = (StatementImpl) si.next();
			on.put(sti.getPredicate().getLocalName(), sti.getObject()
					.toString());
		}
		return ok(on);
	}
}
