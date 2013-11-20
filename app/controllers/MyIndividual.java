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
import utils.QueryUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
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

		for (ExtendedIterator<?> i = oc.listInstances(); i.hasNext();) {
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
	public static Result getProperties(String individualname, String proname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual individual = model.getIndividual(prefix + individualname);

		if (individual == null) {
			return badRequest(JsonUtil.getFalseJson());
		}

		ObjectNode on = Json.newObject();
		if (proname == null) {
			for (StmtIterator si = individual.listProperties(); si.hasNext();) {
				StatementImpl sti = (StatementImpl) si.next();
				on.put(sti.getPredicate().getLocalName(), sti.getObject()
						.toString());
			}
		} else {
			OntProperty op = model.getOntProperty(prefix + proname);
			on.put(proname, individual.getPropertyValue(op).toString());
		}
		return ok(on);
	}

	/**
	 * get individuals by labels
	 * 
	 * @return
	 */
	public static Result getIndivByLabel() {
		JsonNode json = request().body().asJson();

		OntModel model = MyOntModel.getInstance().getModel();

		// Get prefixes
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");

		// Create a new query to find classes which have labels
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?tmp\n" + "WHERE { ";

		JsonNode array = json.findPath("label");
		if (array.isArray()) {
			for (Iterator<JsonNode> it = array.elements(); it.hasNext();) {
				JsonNode label = it.next();
				queryString += "?tmp rdfs:label \"" + label.textValue()
						+ "\"^^<http://www.w3.org/2001/XMLSchema#string>.";
			}
			queryString += "}";
		}

		ResultSet resultSet = QueryUtil.doQuery(model, queryString);

		Boolean isClass = json.path("isClass").asBoolean();
		ObjectNode re = Json.newObject();

		while (resultSet.hasNext()) {
			QuerySolution result = resultSet.nextSolution();
			String name = result.get("tmp").toString();

			// deal with class label
			if (isClass) {
				OntClass oc = model.getOntClass(name);
				for (ExtendedIterator<?> ei = oc.listInstances(); ei.hasNext();) {
					Individual i = (Individual) ei.next();
					ObjectNode tmp = Json.newObject();

					for (StmtIterator st = i.listProperties(); st.hasNext();) {
						StatementImpl sti = (StatementImpl) st.next();
						tmp.put(sti.getPredicate().getLocalName(), sti
								.getObject().toString());
					}
					re.put(i.getLocalName(), tmp);
				}
				// deal with property label
			} else {
				ArrayList<OntClass> classList = new ArrayList<OntClass>();
				OntProperty op = model.getOntProperty(name);
				ExtendedIterator<?> ei = op.listDomain();

				// to get all classes which have the same property
				if (ei.hasNext()) {
					OntClass oc = (OntClass) ei.next();
					if (oc.isUnionClass()) {
						UnionClass unionClass = oc.asUnionClass();
						ei = unionClass.listOperands();
						while (ei.hasNext()) {
							OntClass itemClass = (OntClass) ei.next();
							classList.add(itemClass);
						}
					} else {
						classList.add(model.getOntClass(op.getDomain()
								.toString()));
					}
				}

				for (OntClass tmpClass : classList) {
					for (ExtendedIterator<?> ei2 = tmpClass.listInstances(); ei2
							.hasNext();) {
						Individual individual = (Individual) ei2.next();
						ObjectNode onNode = Json.newObject();
						if ((individual.getPropertyValue(op)) != null) {
							onNode.put(name, individual.getPropertyValue(op)
									.toString());
						}
						re.put(individual.getLocalName(), onNode);
					}
				}
			}
		}
		QueryUtil.closeQE();
		return ok(re);
	}

	/**
	 * remove the individual
	 * 
	 * @param name
	 * @return
	 */
	public static Result remove(String indivname, String proname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual i = model.getIndividual(prefix + indivname);
		if (i == null) {
			return badRequest(JsonUtil.getFalseJson());
		}
		if (proname == null) {
			i.remove();
		} else {
			OntProperty op = model.getOntProperty(prefix + proname);
			if (op != null) {
				i.removeProperty(op, i.getPropertyValue(op));
			} else {
				return badRequest(JsonUtil.getFalseJson());
			}
		}
		// update the model in db
		MyOntModel.getInstance().updateModel(model);
		return ok(JsonUtil.getTrueJson());
	}
}
