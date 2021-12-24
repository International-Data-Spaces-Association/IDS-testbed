package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourcePersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.SelfDescriptionPersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.NullIndexing;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import de.fraunhofer.iais.eis.ids.index.common.persistence.spi.Indexing;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;



public class ResourcePersitenceAndIndexingTest {

    private Model model;
    private ResourcePersistenceAndIndexing resourcePersistenceAndIndexing;
    private Connector exampleConnector;
    private Resource exampleResource1;
    private Resource exampleResource2;
    private Participant exampleParticipant;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BeforeEach
    public void prepare() throws URISyntaxException, IOException, RejectMessageException {


        model = ModelFactory.createDefaultModel();

        exampleResource1 = new DataResourceBuilder(new URI("http://example.org/resource1"))
                ._sovereign_(new URI("http://example.org/participant1"))
                .build();
        exampleResource2 = new DataResourceBuilder(new URI("http://example.org/resource2"))
                ._sovereign_(new URI("http://example.org/participant1"))
                ._defaultRepresentation_(new RepresentationBuilder()
                        ._description_(new TypedLiteral("description", "en"))
                        ._title_(new TypedLiteral("titel", "en"))
                        ._instance_(new ArtifactBuilder()
                                ._checkSum_("12344")
                                ._fileName_("artifact.txt")
                                .build())
                        .build())
                .build();

        exampleParticipant = new ParticipantBuilder(new URI("http://example.org/participant1"))
                ._legalForm_("GmbH")
                .build();

        exampleConnector = new BaseConnectorBuilder(new URI("http://example.org/connector1/"))
                ._maintainer_(new URI("http://example.org/participant1"))
                ._curator_(new URI("http://example.org/participant1"))
                ._outboundModelVersion_("4.0.0")
                ._inboundModelVersion_(Util.asList("4.0.0"))
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder()
                                ._offeredResource_(Util.asList(exampleResource1, exampleResource2))
                        .build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(new URI("https://example.org/endpoint")).build())
                .build();

        Serializer serializer = new Serializer();
        //model.read(new ByteArrayInputStream(serializer.serialize(exampleConnector).getBytes()), null, "JSONLD");
        //model.read(new ByteArrayInputStream(serializer.serialize(resource).getBytes()), null, "JSONLD");
        //model.read(new ByteArrayInputStream(serializer.serialize(exampleParticipant).getBytes()), null, "JSONLD");

        RepositoryFacade repositoryFacade = new RepositoryFacade();
        //RDFConnection connection = repositoryFacade.getNewWritableConnection();
        //connection.load("http://example.org/broker/catalog/824522031", model);
        //connection.close();

        Indexing indexing = new NullIndexing<>();
        SelfDescriptionPersistenceAndIndexing selfDescriptionPersistence = new SelfDescriptionPersistenceAndIndexing(repositoryFacade,
                new URI("http://example.org/broker/catalog/"), indexing);
        selfDescriptionPersistence.updated(exampleConnector);
        resourcePersistenceAndIndexing = new  ResourcePersistenceAndIndexing(repositoryFacade, new URI("http://example.org/broker/catalog/"));
        resourcePersistenceAndIndexing.setIndexing(indexing);
    }


    @Test
    @Disabled
    public void removingBehaviorResourcePersistenceAndIndexingTest() throws IOException, RejectMessageException, URISyntaxException {


        String getExampleParticipantQuery;
        String results;

        getExampleParticipantQuery = "SELECT ?participant ?property ?object WHERE { GRAPH ?g {" +
                "?participant a <https://w3id.org/idsa/core/Participant> ." +
                "?participant ?property ?object ." +
                "}}";
        results = resourcePersistenceAndIndexing.getResults(getExampleParticipantQuery);
        Assertions.assertTrue(results.contains(exampleParticipant.getId().toString()));

        //resourcePersistenceAndIndexing.unavailable(exampleResource.getId(), exampleConnector.getId());
        resourcePersistenceAndIndexing.updated(exampleResource1, exampleConnector.getId());

        getExampleParticipantQuery = "SELECT ?participant ?property ?object WHERE {" +
                "?participant a <https://w3id.org/idsa/core/Participant> ." +
                "?participant ?property ?object ." +
                "}";
        results = resourcePersistenceAndIndexing.getResults(getExampleParticipantQuery);
        Assertions.assertTrue(results.contains(exampleParticipant.getId().toString()));
    }
}
