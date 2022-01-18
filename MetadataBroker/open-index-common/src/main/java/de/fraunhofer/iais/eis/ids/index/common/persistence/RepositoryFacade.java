package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.modify.request.*;
import org.apache.jena.update.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides an interface for easy access to the triple store
 */
public class RepositoryFacade {
    final private Logger logger = LoggerFactory.getLogger(RepositoryFacade.class);
    private URI adminGraphUri;
    private final String graphIsActiveUrl = "https://w3id.org/idsa/core/graphIsActive";
    private String sparqlUrl;
    private Dataset dataset;

    private static boolean writableConnectionWarningPrinted = false;

    private static final String CONNECTOR_QUERY_HATEOS = "PREFIX ids: <https://w3id.org/idsa/core/> \n"
                                                         + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                                                         + "\n"
                                                         + "CONSTRUCT { \n"
                                                         + "  ?s0 ?p0 ?o0 . \n"
                                                         + "  ?o0 ?p1 ?o1 . \n"
                                                         + "} \n"
                                                         + "WHERE {  \n"
                                                         + "  GRAPH <%1$s> {\n"
                                                         + "    { <%1$s> ?p0 ?o0 . } \n"
                                                         + "    UNION \n"
                                                         + "    { ?s owl:sameAs <%1$s> ; ?p0 ?o0 . }\n"
                                                         + "    \n"
                                                         + "    BIND ( IF (BOUND(?s), ?s, <%1$s>) AS ?s0) .\n"
                                                         + "    OPTIONAL { ?o0 ?p1 ?o1 \n"
                                                         + "              \n"
                                                         + "      OPTIONAL { \n"
                                                         + "        { # ?o1 should be an ids:Resource, and only a certain amount shall be returned\n"
                                                         + "          SELECT (?o1 AS ?res) WHERE { GRAPH <%1$s> {\n"
                                                         + "              \n"
                                                         + "                { ?o1 a ids:Resource } UNION { ?o1 a ids:DataResource } UNION { ?o1 a ids:TextResource } UNION { ?o1 a ids:AudioResource } UNION { ?o1 a ids:ImageResource } UNION { ?o1 a ids:VideoResource } UNION { ?o1 a ids:SoftwareResource } UNION { ?o1 a ids:AppResource }\n"
                                                         + "              \n"
                                                         + "            }}\n"
                                                         + "        }\n"
                                                         + "        FILTER ( ?o1 = ?res ) .\n"
                                                         + "      }\n"
                                                         + "    } \n"
                                                         + "  } \n"
                                                         + "}";

    /**
     * Default constructor, creating a local in-memory repository
     */
    public RepositoryFacade() {
        this("");
        try {
            this.adminGraphUri = new URI("https://broker.ids.isst.fraunhofer.de/admin");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor, using a provided SPARQL endpoint as repository
     * @param sparqlUrl the URL of the SPARQL endpoint which is to be used. If this is null or empty, a local in-memory repository will be created
     */
    public RepositoryFacade(String sparqlUrl) {
        if (sparqlUrl == null || sparqlUrl.isEmpty()) {
            logger.info("Preparing memory repository");
            dataset = DatasetFactory.create();
        } else {
            logger.info("Setting SPARQL repository to be used: '" + sparqlUrl + "'");
        }
        try {
            this.adminGraphUri = new URI("https://broker.ids.isst.fraunhofer.de/admin");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.sparqlUrl = sparqlUrl;

        initAdminGraph();
    }


    /**
     * @deprecated This function returns a writable connection. Use the explicit getNewWritableConnection or getNewReadOnlyConnection instead
     * @return Writable RDFConnection to the triplestore
     */
    @Deprecated
    public RDFConnection getNewConnection()
    {
        return getNewWritableConnection();
    }

    /**
     * Utility function for fetching a connection, either to the in-memory repository or the (possibly remote) triple store
     * @return RDFConnection object, providing access to the repository
     */
    public RDFConnection getNewWritableConnection()
    {
        if(sparqlUrl != null && !sparqlUrl.isEmpty()) {
            return RDFConnectionFactory.connectFuseki(sparqlUrl);
            //return RDFConnectionFactory.connectFuseki(sparqlUrl);
        } else
        if(dataset == null)
        {
            dataset = DatasetFactory.create();
        }
        return RDFConnectionFactory.connect(dataset);
    }

    /**
     * This function makes use of the endpoint structure of Fuseki, which offers several endpoints per dataset
     * This will not work with an in-memory setup
     * @return RDFConnection object, which provides a connection to a read-only Fuseki query endpoint
     */
    public RDFConnection getNewReadOnlyConnectionToFuseki()
    {
        if(sparqlUrl != null && !sparqlUrl.isEmpty()) {
            //read only endpoint: host:port/dataset/sparql
            return RDFConnectionFactory.connectFuseki(sparqlUrl + (sparqlUrl.endsWith("/")? "" : "/") + "sparql");
        }
        else
        {
            if(!writableConnectionWarningPrinted)
            {
                logger.warn("Cannot return read-only connection to in-memory dataset. Connection will be writable!");
                logger.warn("This warning is only printed once.");
                writableConnectionWarningPrinted = true;
            }
            return RDFConnectionFactory.connect(dataset);
        }
    }

    /**
     * Utility function to provide a list of context IDs, e.g. the IDs of all known Connectors or Participants
     * @return Collection, containing all context IDs
     */
    public ArrayList<String> getContextIds() {
        ArrayList<QuerySolution> resultSet = selectQuery("SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o . } }");
        return resultSet.stream().map(result -> result.get("g").toString()).collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * Function for adding a collection of statements to a named graph
     * @param statements statements to be added
     * @param namedGraphUri named graph URI to which the statements should be added. Typically, this is the URI of the connector in question
     * @throws RejectMessageException if the named graph doesn't exist
     */
    public void addStatements(Model statements, String namedGraphUri) throws RejectMessageException {
        RDFConnection connection = getNewWritableConnection();
        connection.load(namedGraphUri, statements); //load = add/append, put = set
        if(!namedGraphUri.equals(adminGraphUri.toString()))
        {
            logger.debug("addStatements with an ID which is not the admin graph called. Marking it as available. " + namedGraphUri);
            //Not adding to admin graph, but to a connector/participant graph.
            changePassivationOfGraph(namedGraphUri, true);
        }
        connection.close();
    }

    /**
     * Function to replace ALL statements in a context (named graph) with a new set of statements
     * @param newStatements new set of statements
     * @param namedGraphUri named graph which should be modified
     */
    public void replaceStatements(Model newStatements, String namedGraphUri) throws RejectMessageException {
        //Retrieve a connection
        RDFConnection connection = getNewWritableConnection();

        //Delete all previous statement in this named graph
        Update clearUpdate = new UpdateClear(namedGraphUri);

        //Turn new statements into quads: (named graph, triple)
        List<Quad> newStatementsAsQuad = new ArrayList<>();
        Node namedGraphAsNode = ResourceFactory.createResource(namedGraphUri).asNode();
        StmtIterator iterator = newStatements.listStatements();
        while(iterator.hasNext())
        {
            newStatementsAsQuad.add(new Quad(namedGraphAsNode, iterator.next().asTriple()));
        }

        QuadDataAcc quadDataAcc = new QuadDataAcc(newStatementsAsQuad);
        //Turn insert into an update object
        Update insertUpdate = new UpdateDataInsert(quadDataAcc);

        //Execute the delete
        connection.update(clearUpdate);
        //Execute the insert
        connection.update(insertUpdate);

        //If changes were made to this graph, then it must be available
        if(!namedGraphUri.equals(adminGraphUri.toString()))
        {
            logger.debug("addStatements with an ID which is not the admin graph called. Marking it as available. " + namedGraphUri);
            //Not adding to admin graph, but to a connector/participant graph.
            changePassivationOfGraph(namedGraphUri, true);
        }

        //Cleanup: close connections
        quadDataAcc.close();
        connection.close();
    }

    /**
     * Function to remove a given set of statements from a given named graph
     * @param model Model containing all statements to be removed
     * @param namedGraphUri Named graph from which the statements should be removed
     */
    public void removeStatements(Model model, String namedGraphUri)
    {
        ArrayList<Statement> asList = new ArrayList<>();
        model.listStatements().forEachRemaining(asList::add);
        removeStatements(asList, namedGraphUri);
    }

    /**
     * Function to remove a given set of statements from a given named graph
     * @param statementsToRemove List of statements to be removed
     * @param namedGraphUri Named graph from which the statements should be removed
     */
    public void removeStatements(Iterable<Statement> statementsToRemove, String namedGraphUri)
    {
        if(statementsToRemove == null || !statementsToRemove.iterator().hasNext())
        {
            return;
        }

        //Establish new connection
        RDFConnection connection = getNewWritableConnection();

        //Transform the named graph URI to a node
        Node namedGraphAsNode = ResourceFactory.createResource(namedGraphUri).asNode();

        //Turn the statements we received (triples) into quads: (namedGraphUri, triple)
        List<Quad> statementsAsQuad = new ArrayList<>();
        for(Statement st : statementsToRemove)
        {
            statementsAsQuad.add(new Quad(namedGraphAsNode, st.asTriple()));
        }
        QuadAcc quadAcc = new QuadAcc(statementsAsQuad);

        //Turn this into a delete request
        Update u = new UpdateDeleteWhere(quadAcc);

        //Execute the request
        connection.update(u);

        //Cleanup: close connections
        quadAcc.close();
        connection.close();
    }

    /**
     * Utility function to evaluate an ASK SPARQL query
     * @param query ASK query as String
     * @return Evaluation result (boolean)
     */
    public boolean booleanQuery(String query)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        boolean result = connection.queryAsk(query);
        connection.close();
        return result;
    }

    /**
     * Utility function to evaluate a CONSTRUCT SPARQL query
     * @param query CONSTRUCT query as String
     * @return Evaluation result (graph)
     */
    public Model constructQuery(String query)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        Model m = connection.queryConstruct(query);
        connection.close();
        return m;
    }

    /**
     * Utility function to evaluate a SELECT SPARQL query
     * @param query SELECT query as String
     * @return Evaluation result (List of bindings, tabular form)
     */
    public ArrayList<QuerySolution> selectQuery(String query)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        QueryExecution queryExecution = connection.query(query); //Careful. QueryExecutions MUST BE CLOSED or will cause a freeze, if >5 are left open!!!
        ResultSet resultSet = queryExecution.execSelect();
        ArrayList<QuerySolution> result = new ArrayList<>();
        while(resultSet.hasNext())
        {
            result.add(resultSet.next());
        }
        queryExecution.close();
        return result;
    }

    /**
     * This function executes a select query and returns the result in TSV format
     * @param query The select query to be executed
     * @return Resulting binding as
     */
    public String selectQueryReturnTSV(String query)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        QueryExecution queryExecution = connection.query(query); //Careful. QueryExecutions MUST BE CLOSED or will cause a freeze, if >5 are left open!!!
        ResultSet resultSet = queryExecution.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsTSV(outputStream, resultSet);
        queryExecution.close();
        return outputStream.toString();
    }

    /**
     * Utility function to evaluate a SELECT SPARQL query
     * @param query SELECT query as String
     * @param outputStream Evaluation result is streamed into this output stream
     */
    public void selectQuery(String query, OutputStream outputStream)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        QueryExecution queryExecution = connection.query(query);
        ResultSet resultSet = queryExecution.execSelect();
        ResultSetFormatter.outputAsTSV(outputStream, resultSet);
        queryExecution.close();
    }

    /**
     * Utility function to evaluate a DESCRIBE SPARQL query
     * @param query SELECT query as String
     * @return Evaluation result (graph)
     */
    public Model describeQuery(String query)
    {
        RDFConnection connection = getNewReadOnlyConnectionToFuseki();
        Model m = connection.queryDescribe(query);
        connection.close();
        return m;
    }

    /**
     * This function returns all statements from all active graphs. Note that passive or deleted graphs are not included in the result
     * @return Model containing the statements
     */
    public Model getAllStatements() {
        //Statements are split across different named graphs
        //Iteratively building up the query string Each active graph adds one "FROM NAMED <URL>" clause
        StringBuilder queryStringBuilder = new StringBuilder();

        //CONSTRUCT so that we get a graph as result
        queryStringBuilder.append("CONSTRUCT { ?s ?p ?o . } ");

        //If no graphs exist, return an empty model
        List<String> activeGraphs = getActiveGraphs();
        if(activeGraphs.isEmpty())
        {
            return ModelFactory.createDefaultModel();
        }

        //Only active graphs
        activeGraphs.forEach(graphName -> queryStringBuilder.append("FROM NAMED <").append(graphName).append("> "));

        //Get all statements from these graphs. The GRAPH ?g part is required for any results to be returned
        queryStringBuilder.append(" WHERE GRAPH ?g { ?s ?p ?o . } ");

        //Run the query
        return constructQuery(queryStringBuilder.toString());
    }

    /**
     * Utility function to obtain an IDS Connector object from the triple store
     * @param connectorUri The URI of the connector to be obtained
     * @return an IDS connector object with the requested connectorUri, if it is known to the broker
     * @throws RejectMessageException if the connector is not known to the broker, or if the parsing fails
     */
    public Connector getConnectorFromTripleStore(URI connectorUri) throws RejectMessageException {
        if(!graphIsActive(connectorUri.toString()))
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector with URI " + connectorUri + " is not known to this broker or unavailable."));
        }
        //Fire the query against our repository
        ParameterizedSparqlString queryString = new ParameterizedSparqlString("CONSTRUCT { ?s ?p ?o . }" +
                "WHERE { GRAPH ?g { ?s ?p ?o . } } ");
        queryString.setIri("g", connectorUri.toString());
        try {
            Model result = constructQuery(queryString.toString());

            //Check if response is empty
            if (result.isEmpty()) {
                //Result is empty, throw exception. This will result in a RejectionMessage being sent
                throw new RejectMessageException(RejectionReason.NOT_FOUND);
            }

            //Generate a connector object from the SPARQL result string (already containing the new resource!). This is a bit of a messy business
            return ConstructQueryResultHandler.GraphQueryResultToConnector(result);
        }
        catch (ARQException e)
        {
            logger.warn("Potential SPARQL injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }

    /**
     * Utility function to obtain an IDS Connector object from the triple store
     * @param connectorUri The URI of the connector to be obtained
     * @return an IDS connector object with the requested connectorUri, if it is known to the broker
     * @throws RejectMessageException if the connector is not known to the broker, or if the parsing fails
     */
    public Connector getReducedConnector(URI connectorUri) throws RejectMessageException {
        logger.info("Getting reduced Connector" + connectorUri);
        if(!graphIsActive(connectorUri.toString()))
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector with URI " + connectorUri + " is not known to this broker or unavailable."));
        }
        String rawQueryString = String.format(CONNECTOR_QUERY_HATEOS, connectorUri);
        //Fire the query against our repository
        ParameterizedSparqlString queryString = new ParameterizedSparqlString(rawQueryString);
        try {
            Model result = constructQuery(queryString.toString());

            //Check if response is empty
            if (result.isEmpty()) {
                //Result is empty, throw exception. This will result in a RejectionMessage being sent
                throw new RejectMessageException(RejectionReason.NOT_FOUND);
            }

            //Generate a connector object from the SPARQL result string (already containing the new resource!). This is a bit of a messy business
            return ConstructQueryResultHandler.GraphQueryResultToConnector(result);
        }
        catch (ARQException e)
        {
            logger.warn("Potential SPARQL injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }

    /**
     * Utility function to obtain an IDS Participant object from the triple store
     * @param participantUri The URI of the participant to be obtained
     * @return an IDS Participant object with the requested participantUri, if it is known to the ParIS
     * @throws RejectMessageException if the participant is not known to the ParIS, or if the parsing fails
     */
    public Participant getParticipantFromTripleStore(URI participantUri) throws RejectMessageException {
        if (!graphIsActive(participantUri.toString())) {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector with URI " + participantUri + " is not known to this broker or unavailable."));
        }
        //Fire the query against our repository
        ParameterizedSparqlString queryString = new ParameterizedSparqlString("CONSTRUCT { ?s ?p ?o . }" +
                "WHERE { GRAPH ?g { ?s ?p ?o . } } ");
        queryString.setIri("g", participantUri.toString());
        try {
            Model result = constructQuery(queryString.toString());

            //Check if response is empty
            if (result.isEmpty()) {
                //Result is empty, throw exception. This will result in a RejectionMessage being sent
                throw new RejectMessageException(RejectionReason.NOT_FOUND);
            }

            //Generate a connector object from the SPARQL result string (already containing the new resource!). This is a bit of a messy business
            return ConstructQueryResultHandler.GraphQueryResultToParticipant(result);
        }
        catch (ARQException e)
        {
            logger.warn("Potential SPARQL injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }


    /**
     * Internal function to make sure that the "admin graph" exists. This graph contains information about which named graphs are deleted or passivated
     * Further information might be stored in this graph in the future, such as administration credentials for manual maintenance via an admin web interface
     */
    private void initAdminGraph() {
        //Check if a named graph with the admin URL exists
        logger.info("Admin graph set to " + adminGraphUri.toString());

        logger.debug("Asking whether admin graph exists yet.");
        boolean graphExists = false;
        try {
            graphExists = booleanQuery("ASK WHERE { GRAPH <" + adminGraphUri.toString() + "> {?s ?p ?o .} }");
        }
        catch (QueryExceptionHTTP e)
        {
            if(e.getCause() instanceof HttpHostConnectException) //Did we get something like a connectionRefused error?
            {
                logger.warn("Could not establish connection to " + sparqlUrl + " - changing configuration to use local repository instead");
                sparqlUrl = "";
            }
            else
            {
                throw e;
            }
        }

        if(!graphExists) {
            logger.info("Admin graph does not yet exist. Initializing it with one statement.");
            Model adminGraphModel = ModelFactory.createDefaultModel();
            adminGraphModel.addLiteral(ResourceFactory.createResource(adminGraphUri.toString()), ResourceFactory.createProperty(graphIsActiveUrl), false);
            RDFConnection connection = getNewWritableConnection();
            connection.put(adminGraphUri.toString(), adminGraphModel);
            connection.close();
            //It does not exist - create it (done by setting it to passive, as the fact that it is passive is stored within the admin graph)
            //changePassivationOfGraph(adminGraphUri.toString(), false);
        } else {
            logger.debug("Admin graph found.");
        }
    }

    /**
     * Utility function to return a list of all active (i.e. non-deleted, non-passivated) named graphs
     * Note: This function is often used in the "FROM NAMED" parts in SPARQL queries. In case no active graph exists,
     * one needs to be careful, as having no "FROM NAMED" statement means that all named graphs will be used, including our passivated graphs
     * Hence, special treatment is often required in case this list is empty!
     * @return List of all active (i.e. non-deleted, non-passivated) named graphs
     */
    public List<String> getActiveGraphs()
    {
        ArrayList<QuerySolution> resultSet = selectQuery("SELECT ?graph FROM NAMED <" + adminGraphUri + "> WHERE { GRAPH ?g { ?graph <" + graphIsActiveUrl + "> true . } } ");
        return resultSet.stream().map(result -> result.get("graph").toString()).collect(Collectors.toList());
    }

    /**
     * Utility function to determine whether a given graph is active (i.e. exists and is non-passivated and non-deleted)
     * @param graphUrl The URL of the named graph (i.e. connector / participant URI) to be queried
     * @return true, if the corresponding graph is active, otherwise false
     */
    public boolean graphIsActive(String graphUrl) throws RejectMessageException {
        //Query admin graph, check if a triple satisfying "URI isActive true" exists
        logger.debug("Asking whether graph " + graphUrl + " is active.");
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString("ASK FROM NAMED <" + adminGraphUri.toString() + "> WHERE { GRAPH ?g { ?connector <" + graphIsActiveUrl + "> true . } } ");
        parameterizedSparqlString.setIri("connector", graphUrl);
        try {
            return booleanQuery(parameterizedSparqlString.toString());
        }
        catch (ARQException e)
        {
            logger.info("Potential injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }

    /**
     * Internal function to remove all statements about a named graph from the admin graph. This is required when a graph changes states
     * @param graphUrl Graph URL which should be removed from the admin graph
     */
    private void removeGraphFromAdminGraph(String graphUrl) throws RejectMessageException {
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString("CONSTRUCT { ?s ?p ?o . } FROM NAMED <" + adminGraphUri.toString() + "> WHERE { GRAPH <" + adminGraphUri.toString() + "> { ?s ?p ?o } }");
        parameterizedSparqlString.setIri("s", graphUrl);
        try {
            Model constructSolution = constructQuery(parameterizedSparqlString.toString());
            /*ArrayList<Statement> solutionAsStatements = new ArrayList<>();
            for (QuerySolution solution : selectSolution) {
                solutionAsStatements.add(ResourceFactory.createStatement(solution.getResource("s"), ResourceFactory.createProperty(solution.get("p").toString()), solution.get("o")));
            }*/

            removeStatements(constructSolution, adminGraphUri.toString());
        }
        catch (ARQException e)
        {
            logger.warn("Potential SPARQL injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }

    /**
     * This function can be used to change whether a named graph should be considered active or passive, such as when a ConnectorInactiveMessage is received
     * @param graphUrl The URL of the named graph (i.e. the connector / participant URL)
     * @param active The new state (true = active, false = inactive) of the named graph
     * @throws RejectMessageException can be thrown, if for example the named graph does not exist
     */
    public void changePassivationOfGraph(String graphUrl, boolean active) throws RejectMessageException {
        if(!graphExists(graphUrl) && !graphUrl.equals(adminGraphUri.toString()))
        {
            //Graph does not yet exist. Only allow this, if the graph should now be available (i.e. a new registration)
            if(!active)
                throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The graph does not exist"));
        }
        //At this point, the graph either exists, or we want to store for a new graph that it is now active
        logger.info("Changing passivation of graph " + graphUrl + ". Is now active: " + active);
        removeGraphFromAdminGraph(graphUrl);

        Model newStatements = ModelFactory.createDefaultModel();
        newStatements.addLiteral(ResourceFactory.createResource(graphUrl), ResourceFactory.createProperty(graphIsActiveUrl), active);
        addStatements(newStatements, adminGraphUri.toString());
    }

    /**
     * Function to test whether a graph exists (active or passive)
     * @param graphUrl URL of the graph to be tested
     * @return true, if the graph exists (can be passivated), false otherwise
     */
    public boolean graphExists(String graphUrl) throws RejectMessageException {
        logger.debug("Asking whether graph " + graphUrl + " exists.");

        //Can we find the graph in the triplestore? If yes, then it exists and was not deleted
        try {
            ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString("ASK FROM NAMED <" + graphUrl + "> WHERE { GRAPH ?g { ?s ?p ?o . } } ");
            parameterizedSparqlString.setIri("g", graphUrl);
            return booleanQuery(parameterizedSparqlString.toString());
        }
        catch (ARQException e)
        {
            logger.info("Potential injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }


    /**
     * This function gets the number of active graphs
     * @return Number of named graphs in the triple store
     */
    public int getSize() {
        return getActiveGraphs().size();
    }

}
