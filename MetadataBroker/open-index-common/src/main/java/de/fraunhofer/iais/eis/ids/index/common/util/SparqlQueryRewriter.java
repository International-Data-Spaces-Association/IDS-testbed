package de.fraunhofer.iais.eis.ids.index.common.util;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This utility class reformulates incoming SPARQL requests. If the query contains a FROM section, it is denied.
 * The reformulated query will hide the complexity of named graphs, creating a union graph of all ACTIVE graphs (this requires a repository facade connection)
 */
public class SparqlQueryRewriter {

    private static final Logger logger = LoggerFactory.getLogger(SparqlQueryRewriter.class);

    /**
     * Query reformulation function, rejecting malformed queries and queries with FROM part.
     * Queries are reformulated to respect only active graphs (requires repository connection)
     * @param queryString Query to be reformulated
     * @param repositoryFacade Repository facade from which a list of active graphs can be queried
     * @throws RejectMessageException thrown, if the query is malformed, the index is empty or if the query contains a FROM clause
     */
    public static String reformulate(String queryString, RepositoryFacade repositoryFacade) throws RejectMessageException
    {
        //Parse the query (using Apache Jena)
        Query query;
        try {
            query = QueryFactory.create(queryString);
        }
        catch (Exception e) //Catch clause in case parsing fails
        {
            logger.info("Malformed query received.", e);
            throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, e);
        }
        //Test if a FROM clause was used (first one tests for a URI for default graph, second for named graphs)
        if(query.getGraphURIs().size() > 0 || query.getNamedGraphURIs().size() > 0)
        {
            logger.info("FROM clause detected. Rejecting query");
            //A FROM clause was specified. Reject query
            throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new Exception("FROM clause is not allowed in queries."));
        }
        //No FROM clause was specified. Attach our own FROM clauses

        List<String> activeGraphs = repositoryFacade.getActiveGraphs();
        //We need to make sure at least one active graph exists. Otherwise no "FROM NAMED <URI>" is generated, allowing the user to query inactive graphs or the admin graph
        if(activeGraphs.isEmpty())
        {
            //TODO: It would be better to return an empty result set instead. But this really depends on whether it is a SELECT, CONSTRUCT, ASK, ... Query.
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The index is empty - your query could not be evaluated."));
        }
        for(String graph : activeGraphs)
        {
            //Attach all active graphs in FROM clause
            query.addNamedGraphURI(graph);
        }

        //Next up, we need to do some string operations
        String serializedQuery = query.serialize();
        int whereIndex = serializedQuery.indexOf("WHERE");
        if(whereIndex > 0)
        {
            //Note that the parsing and serializing takes care of some pretty printing. Keywords are always fully capitalized
            //selECT ?s ?p ?o WHERE {grAph?g{ ?s ?p ?o } } is rewritten to SELECT ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } } (with some line breaks and spacing)
            //This means that the following string operation works as expected
            if(!serializedQuery.substring(whereIndex).contains(" GRAPH ?"))
            {
                //Get the index of the first opening bracket after WHERE
                int bracketIndex = serializedQuery.indexOf("{", whereIndex);

                //Surround the WHERE part with a GRAPH ?__RESERVED { ... }
                serializedQuery = serializedQuery.substring(0, bracketIndex + 1) + " GRAPH ?__RESERVED { " + serializedQuery.substring(bracketIndex + 1) + " } ";
                logger.debug("Added GRAPH section to query. Resulting query:\n" + serializedQuery);
            }
            //No else part required, if the query already contains a GRAPH ?g { ... } wrapper
        }
        else
        {
            throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Your query must contain a WHERE part."));
        }

        return serializedQuery;
    }
}
