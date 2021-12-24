package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Class which provides functionality to generate an ids:Catalog object directly from the triplestore.
 * Visibility is respected, meaning that deleted and inactive connectors are excluded
 */
public class CatalogProvider {

    Logger logger = LoggerFactory.getLogger(CatalogProvider.class);
    RepositoryFacade repositoryFacade;
    String catalogUri;

    /**
     * The single constructor of this class
     * @param repositoryFacade The repository from which the catalog should be retrieved
     * @param catalogUri The URI the resulting catalog is supposed to have
     */
    public CatalogProvider(RepositoryFacade repositoryFacade, String catalogUri)
    {
        this.repositoryFacade = repositoryFacade;
        this.catalogUri = catalogUri;
    }

    /**
     * Function to generate the catalog from the triplestore
     * @return Catalog object, reflecting current state of the triplestore, excluding inactive or deleted resources
     * @throws RejectMessageException if the catalog could not be retrieved, e.g. if the triplestore is unreachable or if the serializer encounters an exception during the parse process
     */
    public ResourceCatalog generateCatalogFromTripleStore() throws RejectMessageException {
        //TODO: only offer so far, not request
        try {
            List<String> activeGraphs = repositoryFacade.getActiveGraphs();
            if(activeGraphs.isEmpty())
            {
                //Make sure that nothing is returned in case of all graphs being inactive
                return new ResourceCatalogBuilder().build();
            }
            //Iteratively build up this monstrous query string
            StringBuilder queryString = new StringBuilder();
            queryString.append("PREFIX ids: <https://w3id.org/idsa/core/> ");

            //We need a CONSTRUCT query to get a graph as result
            //The many optionals are required to achieve "get all nodes up to X hops away"
            //TODO: Instead of using optionals, this can also be done by using unions, see: http://www.snee.com/bobdc.blog/2014/10/dropping-optional-blocks-from.html
            queryString.append("CONSTRUCT { <").append(catalogUri).append("> a ids:ResourceCatalog . <").append(catalogUri).append("> ids:offeredResource ?resource . ?resource ?p ?o . ?o ?p2 ?o2 . ?o2 ?p3 ?o3 . ?o3 ?p4 ?o4 . ?o4 ?p5 ?o5 . ?o5 ?p6 ?o6 . ?o6 ?p7 ?o7 . ?o7 ?p8 ?o8 . ?o8 ?p9 ?o9 . ?o9 ?p10 ?o10 . ?o10 ?p11 ?o11 . ?o11 ?p12 ?o12 . } ");

            //Only include active graphs. Passivated graphs (or deleted graphs) need to be excluded from catalog
            activeGraphs.forEach(graphName -> queryString.append("FROM NAMED <").append(graphName).append("> "));

            //The WHERE part corresponding to the CONSTRUCT section
            //Note that even the resource is optional. This prevents an error in case no resource is known yet
            queryString.append("WHERE { GRAPH ?g { OPTIONAL { ?catalog ids:offeredResource ?resource . ?resource ?p ?o . OPTIONAL { ?o ?p2 ?o2 . OPTIONAL { ?o2 ?p3 ?o3 . OPTIONAL { ?o3 ?p4 ?o4 . OPTIONAL { ?o4 ?p5 ?o5 . OPTIONAL { ?o5 ?p6 ?o6 . OPTIONAL { ?o6 ?p7 ?o7 . OPTIONAL { ?o7 ?p8 ?o8 . OPTIONAL { ?o8 ?p9 ?o9 . OPTIONAL { ?o9 ?p10 ?o10 . OPTIONAL { ?o10 ?p11 ?o11 . OPTIONAL { ?o11 ?p12 ?o12 . } } } } } } } } } } } } } } ");

            //Fire query
            Model result = repositoryFacade.constructQuery(queryString.toString());

            //Transform the result to a collection, then turn it into JSON-LD and give it to the serializer for parsing
            return new Serializer().deserialize(ConstructQueryResultHandler.graphToString(result), ResourceCatalog.class);
        }
        catch (Exception e)
        {
            logger.error("An error occurred while trying to retrieve own catalog", e);
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }
}
