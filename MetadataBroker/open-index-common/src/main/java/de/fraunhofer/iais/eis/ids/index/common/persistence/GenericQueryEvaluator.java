package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.util.SparqlQueryRewriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

import java.io.ByteArrayOutputStream;

/**
 * This class can evaluate incoming SPARQL queries. Queries are rewritten to not expose information from inactive connectors
 */
public class GenericQueryEvaluator {

    public final RepositoryFacade repositoryFacade;

    /**
     * Constructor
     * @param repositoryFacade RepositoryFacade, providing access to the triple store
     */
    public GenericQueryEvaluator(RepositoryFacade repositoryFacade)
    {
        this.repositoryFacade = repositoryFacade;
    }

    /**
     * Evaluate a given query, which will be rewritten within this function
     * @param queryString Original query to be evaluated
     * @return Result of a rewritten query
     * @throws RejectMessageException if the query cannot be evaluated, or if it is not of ASK, SELECT, CONSTRUCT, or DESCRIBE type
     */
    public String getResults(String queryString) throws RejectMessageException {
        //Evaluate the reformulated query
        String reformulatedQuery = SparqlQueryRewriter.reformulate(queryString, repositoryFacade);
        Query originalQuery = QueryFactory.create(queryString);
        //Determine the type of query. Depending on it, we will receive different result formats
        //SELECT query provides a tabular result. Returning variable bindings in table form
        if(originalQuery.isSelectType())
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            repositoryFacade.selectQuery(reformulatedQuery, outputStream);
            return new String(outputStream.toByteArray());
        }
        //DESCRIBE provides an RDF Graph as result, see https://www.w3.org/TR/rdf-sparql-query/#describe
        if(originalQuery.isDescribeType())
        {
            return ConstructQueryResultHandler.graphToString(repositoryFacade.describeQuery(reformulatedQuery));
        }
        //CONSTRUCT also provides an RDF Graph as result. Here, the returned structure is explicitly provided by the query
        if(originalQuery.isConstructType())
        {
            return ConstructQueryResultHandler.graphToString(repositoryFacade.constructQuery(reformulatedQuery));
        }
        //ASK returns a boolean value
        if(originalQuery.isAskType())
        {
            return String.valueOf(repositoryFacade.booleanQuery(reformulatedQuery));
        }
        //Other query types are not supported. Particularly, this rejects DELETE queries
        throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new Exception("Could not determine query type from SPARQL query (ASK, SELECT, CONSTRUCT, DESCRIBE)"));
    }
}
