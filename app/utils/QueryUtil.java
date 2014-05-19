package utils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

public class QueryUtil {

	public static QueryExecution qe;

	/**
	 * do query
	 * 
	 * @param model
	 * @param queryStr
	 * @return
	 */
	public static ResultSet doQuery(OntModel model, String queryStr) {
		// Create the query
		Query query = QueryFactory.create(queryStr);
		// Execute the query and obtain results
		qe = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = qe.execSelect();

		return resultSet;
	}

	/**
	 * close the QueryExecution to free up resources used running the query
	 * 
	 * @param qe
	 */
	public static void closeQE() {
		if (qe != null) {
			qe.close();
		}
	}
}
