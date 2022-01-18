package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.Broker;
import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ParIS;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.ARQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to provide IDS descriptions of known object, such as registered connectors, participants, a self description of the broker/ParIS itself or its catalog
 */
public class DescriptionProvider {

    final Logger logger = LoggerFactory.getLogger(DescriptionProvider.class);
    //For describing "foreign objects"
    RepositoryFacade repositoryFacade;
    //For describing itself
    InfrastructureComponent selfDescription;

    CatalogProvider catalogProvider;

    URI catalogUri;

    /**
     * Constructor
     * @param selfDescription Pass the infrastructure component representing the service provider (broker/ParIS). Required to provide a self description etc.
     * @param repositoryFacade Repository/Triplestore from which information about objects can be retrieved for generating descriptions
     * @param catalogUri The URI of the catalog of the broker (ParIS might not apply here). Required to recognize that the full catalog might have been requested
     */
    public DescriptionProvider(InfrastructureComponent selfDescription, RepositoryFacade repositoryFacade, URI catalogUri){
        this.selfDescription = selfDescription;
        this.repositoryFacade = repositoryFacade;
        if(catalogUri != null) {
            this.catalogProvider = new CatalogProvider(repositoryFacade, catalogUri.toString());
            this.catalogUri = catalogUri;
        }

    }

    /**
     * The single function to get the description of an element in the desired serialization
     * @param requestedElement The URI of the element to be described
     * @param desiredLanguage The serialization in which the RDF should be returned
     * @return A JSON-LD description of the object, if it is known to the broker/ParIS
     * @throws RejectMessageException thrown if the requested object is not known or could not be retrieved
     */
    public String getElement(URI requestedElement, Lang desiredLanguage) throws RejectMessageException {
        return getElement(requestedElement, 0, desiredLanguage);
    }

    /**
     * The single function to get the description of an element in the desired serialization
     * @param requestedElement The URI of the element to be described
     * @param depth The depth to which we should show child elements
     * @param desiredLanguage The serialization in which the RDF should be returned
     * @return A JSON-LD description of the object, if it is known to the broker/ParIS
     * @throws RejectMessageException thrown if the requested object is not known or could not be retrieved
     */
    public String getElement(URI requestedElement, int depth, Lang desiredLanguage) throws RejectMessageException {

        //Check if a self description is requested
        if(requestedElement == null || requestedElement.equals(selfDescription.getId()))
        {
            logger.info("Self-description has been requested");
            return selfDescription.toRdf();
        }
        StringBuilder queryString = new StringBuilder();
        queryString.append("PREFIX ids: <https://w3id.org/idsa/core/> \n");
        queryString.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"); //sameAs statements

        //Check if we are at the root. This top path, which is the catalog URI to the outside, is not persisted as such in the triple store, but generated upon request
        //If the root URI has been requested, we need to create specific SPARQL CONSTRUCT queries to serve a connector catalog
        boolean atRoot = false;

        if(requestedElement.equals(catalogUri) || (requestedElement.toString() + "/").equals(catalogUri.toString()))
        {
            logger.info("Catalog has been requested (with depth: " + depth + "): " + requestedElement);
            atRoot = true;
            if(selfDescription instanceof ParIS)
            {
                queryString.append("CONSTRUCT { <").append(catalogUri).append("> a ids:ParticipantCatalog ; ids:member ?s0 . ?s0 ?p0 ?o0 .");
            }
            else if(selfDescription instanceof Broker)
            {
                queryString.append("CONSTRUCT { <").append(catalogUri).append("> a ids:ConnectorCatalog ; ids:listedConnector ?s0 . ?s0 ?p0 ?o0 .");
            }
            else
            {
                throw new RuntimeException("Could not determine which catalog type should be returned.");
            }
        }
        else
        {
            logger.info("Custom element has been requested (with depth  " + depth + "): " + requestedElement);
            queryString.append("CONSTRUCT { ?s0 ?p0 ?o0 . ");
        }

        for(int i = 0; i < depth; i++)
        {
            queryString.append("?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" . ");
        }

        //Close CONSTRUCT brackets
        queryString.append(" } ");
        List<String> activeGraphs = repositoryFacade.getActiveGraphs();
        activeGraphs.forEach(graphName -> queryString.append("FROM NAMED <").append(graphName).append("> "));
        if(activeGraphs.isEmpty())
        {
            //Make sure that there is a FROM statement. If there is no FROM statement, it is assumed that ALL graphs should be used
            //This will result in an empty result set, except for the few triples we have constructed above
            queryString.append("FROM NAMED <http://dummy.org/non-existing-graph> ");
        }
        queryString.append("WHERE { ");

        if(!atRoot) //Specific element was requested, which we can retrieve "as-is" (unlike the catalog, which we need to generate on the fly)
        {
            //first ?s0 ?p0 ?o0 NOT in optional block. If unknown resource is requested, error should be thrown

            //Do not explicitly bind the requestedElement. Instead, do this via Parameterised Sparql String for security
            //queryString.append("BIND(<").append(requestedElement.toString())
            //        //Also include owl:sameAs equivalent objects
            //        .append("> AS ?requestedElement) .");
            queryString.append(" GRAPH ?g {  { ?requestedElement ?p0 ?o0 . } UNION { ?s owl:sameAs ?requestedElement ; ?p0 ?o0 . } ")
                    .append(" BIND ( IF (BOUND(?s), ?s, ?requestedElement) AS ?s0) ."); //Make sure that the rewritten URI is used as s0
        }


        else //Need to construct a catalog, so we query for all active connectors / participants we know
        {
            if(selfDescription instanceof Broker)
                //First ?s0 ?p0 ?o0 IS in optional block. If catalog is empty, no error should be thrown
                //At this stage, the ontology class hierarchy is not respected in queries. Therefore, we will list all Connector types
                queryString.append("GRAPH ?g { OPTIONAL { ?s0 ?p0 ?o0 . ?s0 a ?s0type . FILTER( ?s0type IN ( ids:BaseConnector, ids:TrustedConnector, ids:Connector ) ) . ");
            else
                //We're a ParIS
                queryString.append("GRAPH ?g { OPTIONAL { ?s0 ?p0 ?o0 . ?s0 a ?s0type . FILTER( ?s0type = ids:Participant ) . ");
        }

        for(int i = 0; i < depth; i++)
        {
            queryString.append("OPTIONAL { ?o").append(i).append(" ?p").append(i + 1).append(" ?o").append(i + 1).append(" ");
        }
        queryString.append("} ".repeat(Math.max(0, depth)));

        queryString.append("} }"); //Brackets from graph and where

        if(atRoot)
        {
            //At root, there is one more OPTIONAL
            queryString.append(" }");
        }

        //Fire construct query against triple store
        Model result;
        if(!atRoot)
        {
            try {
                ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(queryString.toString());
                parameterizedSparqlString.setIri("requestedElement", requestedElement.toString());
                result = repositoryFacade.constructQuery(parameterizedSparqlString.toString());
            }
            catch (ARQException e)
            {
                logger.warn("Potential SPARQL injection attack detected.", e);
                throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
            }
        }
        else
        {
            result = repositoryFacade.constructQuery(queryString.toString());
        }

        //Check if requested element exists in our persistence
        if(result.isEmpty())
        {
            //Result is empty, throw exception. This will result in a RejectionMessage being sent
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The requested resource was not found"));
        }

        //Turn the result into a string and return
        return ConstructQueryResultHandler.graphToString(result, desiredLanguage);

    }

    public String getTypeOfRequestedElement(URI requestedElement) throws RejectMessageException {
        if(requestedElement == null || requestedElement.equals(selfDescription.getId()))
        {
            //TODO: More specific subclasses
            return "https://w3id.org/idsa/core/Connector";
        }
        if(requestedElement.equals(catalogUri) || (requestedElement.toString() + "/").equals(catalogUri.toString()))
        {
            //TODO: ConnectorCatalog or ResourceCatalog?
            return "https://w3id.org/idsa/core/Catalog";
        }
        StringBuilder queryString = new StringBuilder();
        queryString.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"); //sameAs statements
        queryString.append("SELECT ?type ");
        List<String> activeGraphs = repositoryFacade.getActiveGraphs();
        if(activeGraphs.isEmpty())
        {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("Could not retrieve type of " + requestedElement));
        }
        for(String activeGraph : activeGraphs)
        {
            queryString.append("FROM NAMED <").append(activeGraph).append("> ");
        }
        queryString.append(" WHERE { GRAPH ?g { { ?s a ?type . } UNION { ?s0 owl:sameAs ?s . ?s0 a ?type . } } } ");
        ParameterizedSparqlString parameterizedSparqlString = new ParameterizedSparqlString(queryString.toString());
        parameterizedSparqlString.setIri("s", requestedElement.toString());
        try {
            ArrayList<QuerySolution> result = repositoryFacade.selectQuery(parameterizedSparqlString.toString());
            if (result == null || result.isEmpty()) {
                throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("Could not retrieve type of " + requestedElement));
            }

            if (result.size() > 1) {
                RejectMessageException e = new RejectMessageException(RejectionReason.TOO_MANY_RESULTS, new Exception("Could not determine type of " + requestedElement + " (multiple options)"));
                logger.error("Could not determine the type of a requested element.", e);
                throw e;
            }
            return result.get(0).get("type").toString();
        }
        catch (ARQException e)
        {
            logger.warn("Potential SPARQL injection attack detected.", e);
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE);
        }
    }

}
