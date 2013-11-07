package utils;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class ModelUtil {

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
			// true means list all properties except relation(ObjectProperty)
			for (ExtendedIterator i = oc.listDeclaredProperties(); i.hasNext();) {
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
			for (ExtendedIterator i = oc.listDeclaredProperties(); i.hasNext();) {
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
			String value = json.findPath(tmp).textValue();
			if (value != null) {
				i.addProperty(op, pre + json.findPath(tmp).textValue());
			}
		}

		MyOntModel.getInstance().updateModel(model);

		return true;
	}

}
