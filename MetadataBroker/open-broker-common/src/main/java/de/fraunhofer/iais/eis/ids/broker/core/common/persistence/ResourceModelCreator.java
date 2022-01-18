package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.INFOMODEL;
import de.fraunhofer.iais.eis.ids.index.common.persistence.ModelCreator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import java.net.URI;
import java.util.Optional;

/**
 * This class is an extension to the ModelCreator class, providing functionality to determine whether an RDF triple defines a Resource
 */
public class ResourceModelCreator extends ModelCreator {

    private URI connectorUri;

    /**
     * Setter method for the URI of the connector holding this resource
     * @param connectorUri URI of the connector holding this resource
     * @return The updated ResourceModelCreator
     */
    public ResourceModelCreator setConnectorUri(URI connectorUri)
    {
        this.connectorUri = connectorUri;
        return this;
    }

    /**
     * This function determines whether a statement defines a Resource
     * @param statement The statement to be tested
     * @return true, if it defines a Resource, otherwise false
     */
    @Override
    public boolean subjectIsInstanceInnerModel(Statement statement) {
        return statement.getPredicate().equals(RDF.type) &&
                        statement.getObject().equals(INFOMODEL.RESOURCE);
    }

    /**
     * For resources, the named graph is NOT the resource URI, but the URI of the connector holding the resource, hence we need to override this function
     * @param model Model for which the named graph should be determined
     * @return URI, wrapped in an RDF4J Resource, of the connector holding this resource
     * @throws RejectMessageException thrown, if the named graph could not be determined, e.g. if the connector is not known
     */
    //Resources are stored in the same named graph as the connector offering them
    @Override
    public Optional<Resource> determineNamedGraph(Model model) throws RejectMessageException {
        if(connectorUri == null)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, new NullPointerException("Could not determine the named graph of the resource"));
        }
        return Optional.of(ResourceFactory.createResource(connectorUri.toString()));
    }

}
