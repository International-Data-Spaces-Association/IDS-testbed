package de.fraunhofer.iais.eis.ids.index.common.persistence;


import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Class which provides some static references to some selected IDS classes
 */
public class INFOMODEL {

    final static String NS = "https://w3id.org/idsa/core/";

    public final static Resource BASE_CONNECTOR = ResourceFactory.createResource(NS + "BaseConnector");
    public final static Resource TRUSTED_CONNECTOR = ResourceFactory.createResource(NS + "TrustedConnector");
    public final static Resource PARTICIPANT = ResourceFactory.createResource(NS + "Participant");
    public final static Resource RESOURCE = ResourceFactory.createResource(NS + "Resource");

}
