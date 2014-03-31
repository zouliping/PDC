package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class ModelUtil {

	/**
	 * read owl file, create the ontModel, and store in db
	 * 
	 * @param conn
	 * @return
	 */
	public static OntModel createModel(IDBConnection conn) {
		ModelMaker maker = ModelFactory.createModelRDBMaker(conn);
		Model model = maker.createModel("pdc");

		try {
			File file = new File("./owl/OntologyPDC_test.owl");
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			model.read(isr, null);

			model.commit();

			isr.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		OntModelSpec spec = new OntModelSpec(
				OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		return ModelFactory.createOntologyModel(spec, model);
	}

	/**
	 * get a specific class's properties
	 * 
	 * @param classname
	 * @return
	 */
	public static ArrayList<String> getPropertyList(String classname) {
		ArrayList<String> list = new ArrayList<String>();
		OntClass oc = getExistedClass(classname);

		if (oc == null) {
			return null;
		} else {
			// true means listing all properties except relation(ObjectProperty)
			for (ExtendedIterator<OntProperty> i = oc.listDeclaredProperties(); i
					.hasNext();) {
				OntProperty op = (OntProperty) i.next();

				if (op != null) {
					list.add(op.getLocalName());
				}
			}
			return list;
		}
	}

	/**
	 * get a specific class's properties
	 * 
	 * @param oc
	 * @return
	 */
	public static ArrayList<String> getPropertyList(OntClass oc) {
		ArrayList<String> list = new ArrayList<String>();

		if (oc == null) {
			return null;
		} else {
			for (ExtendedIterator<OntProperty> i = oc.listDeclaredProperties(); i
					.hasNext();) {
				OntProperty op = (OntProperty) i.next();

				if (op != null) {
					list.add(op.getLocalName());
				}
			}
			return list;
		}
	}

	/**
	 * get a existed class
	 * 
	 * @param classname
	 * @return
	 */
	public static OntClass getExistedClass(String classname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		OntClass oc = model.getOntClass(prefix + classname);
		return oc;
	}

	/**
	 * get a existed individual
	 * 
	 * @param individualname
	 * @return
	 */
	public static Individual getExistedIndividual(String individualname) {
		OntModel model = MyOntModel.getInstance().getModel();
		String prefix = model.getNsPrefixURI("");
		Individual i = model.getIndividual(prefix + individualname);
		return i;
	}

	/**
	 * add individual's properties
	 * 
	 * @param oc
	 * @param i
	 * @param proList
	 * @param json
	 * @return
	 */
	public static Boolean addIndividualProperties(OntClass oc, Individual i,
			ArrayList<String> proList, JsonNode json) {
		OntModel model = MyOntModel.getInstance().getModel();
		String pre = model.getNsPrefixURI("");
		OntProperty op;

		for (String tmp : proList) {
			op = model.getOntProperty(pre + tmp);

			if (op != null) {
				String value = json.findPath(tmp).textValue();
				if (value != null) {
					System.out.println("op:" + tmp + "   value:" + value);
					if ((i.getPropertyValue(op)) != null) {
						i.removeProperty(op, i.getPropertyValue(op));
					}
					i.addProperty(op, value);
				} else {
					value = json.findPath(tmp).booleanValue() + "";
					System.out.println("op:" + tmp + "   value:" + value);
					if ((i.getPropertyValue(op)) != null) {
						i.removeProperty(op, i.getPropertyValue(op));
					}
					i.addProperty(op, value);
				}
			}
		}

		MyOntModel.getInstance().updateModel(model);
		return true;
	}

	/**
	 * add individual's properties
	 * 
	 * @param oc
	 * @param i
	 * @param proIterator
	 * @param json
	 * @return
	 */
	public static Boolean addIndividualProperties(OntClass oc, Individual i,
			Iterator<String> proIterator, JsonNode json) {
		OntModel model = MyOntModel.getInstance().getModel();
		String pre = model.getNsPrefixURI("");
		OntProperty op;
		String tmp;

		while (proIterator.hasNext()) {
			tmp = proIterator.next();
			op = model.getOntProperty(pre + tmp);

			if (op != null) {
				String value = json.findPath(tmp).textValue();

				if (value != null) {
					// Charset.forName("UTF-8").encode(value);
					i.addProperty(op, value);
				}
			}
		}
		MyOntModel.getInstance().updateModel(model);
		return true;
	}

	/**
	 * get a class name by individual name
	 * 
	 * @param id
	 * @return
	 */
	public static String getClassname(String id) {
		OntModel model = MyOntModel.getInstance().getModel();
		String pre = model.getNsPrefixURI("");
		String rdfPre = model.getNsPrefixURI("rdf");
		Individual individual = model.getIndividual(pre + id);

		if (individual == null) {
			return null;
		}

		OntProperty op = model.getOntProperty(rdfPre + "type");
		String[] tmp = individual.getPropertyValue(op).toString().split("#");
		if (tmp.length == 2) {
			return tmp[1];
		} else {
			return null;
		}
	}

	/**
	 * get relation
	 * 
	 * @param classname1
	 * @param classname2
	 * @return
	 */
	public static String getRelation(String classname1, String classname2) {
		OntModel model = MyOntModel.getInstance().getModel();
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");

		// Create a new query
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?relation\n"
				+ "WHERE { ?relation rdfs:domain default:" + classname1
				+ ".?relation rdfs:range default:" + classname2 + "}";

		ResultSet results = QueryUtil.doQuery(model, queryString);

		// Get property value
		String relationValue;
		if (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			relationValue = result.get("relation").toString();
		} else {
			relationValue = null;
		}

		QueryUtil.closeQE();
		return relationValue;
	}

	/**
	 * get followers
	 * 
	 * @return
	 */
	public static ArrayList<String> getFollowers(String uid) {
		OntModel model = MyOntModel.getInstance().getModel();
		String defaultPrefix = model.getNsPrefixURI("");
		String rdfsPrefix = model.getNsPrefixURI("rdfs");
		String owlPrefix = model.getNsPrefixURI("owl");
		ArrayList<String> users = new ArrayList<String>();

		// Create a new query
		String queryString = "PREFIX default: <" + defaultPrefix + ">\n"
				+ "PREFIX rdfs: <" + rdfsPrefix + ">\n" + "PREFIX owl: <"
				+ owlPrefix + ">\n" + "SELECT ?user\n" + "WHERE { default:"
				+ uid + " default:follow ?user }";

		ResultSet results = QueryUtil.doQuery(model, queryString);

		while (results.hasNext()) {
			QuerySolution result = results.nextSolution();
			String user = result.get("user").toString();
			System.out.println(user);
			users.add(user);
		}
		return users;
	}

	/**
	 * To judge is user's individual
	 * 
	 * @param i
	 * @return
	 */
	public static Boolean isUserIndiv(Individual i, String uid) {
		if (i.hasLabel(uid, null)
				|| i.hasLabel(
						(uid + "^^<http://www.w3.org/2001/XMLSchema#string>"),
						null)) {
			return true;
		} else {
			return false;
		}
	}
}
