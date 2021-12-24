package de.fraunhofer.iais.eis.ids.broker.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.SelfDescriptionPersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.NullIndexing;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class RdfPersistenceTest {

    private SelfDescriptionPersistenceAndIndexing persistence;
    private RepositoryFacade repositoryFacade;

    @Before
    public void setUp() {
        repositoryFacade = new RepositoryFacade();
        try {
            persistence = new SelfDescriptionPersistenceAndIndexing(repositoryFacade, new URI("http://localhost:8080/connectors/"), new NullIndexing<>());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void available() {
        long sizeBeforeIngest = repositoryFacade.getSize();
        try {
            persistence.updated(createConnector());
            persistence.updated(createConnector());

            Assert.assertTrue(sizeBeforeIngest < repositoryFacade.getSize());
            Assert.assertEquals(3, repositoryFacade.getContextIds().size());
        }
        catch (IOException | RejectMessageException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private InfrastructureComponent createConnector() {
        URI curator = null;
        URI maintainer = null;
        try {
            curator = new URI("http://example.org/curator");
            maintainer = new URI("http://example.org/maintainer");
        }
        catch (URISyntaxException ignored) {
            // shouldn't happen
        }
        return new BaseConnectorBuilder()
                ._title_(new ArrayList<>(Collections.singletonList(new TypedLiteral("DWD Open Data Connector"))))
                ._curator_(curator)
                ._maintainer_(maintainer)
                ._outboundModelVersion_("4.0.0")
                ._inboundModelVersion_(asList("4.0.0"))
                ._resourceCatalog_(asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

    }

    @Test
    public void update() {
        BaseConnectorImpl connector = (BaseConnectorImpl) createConnector();
        try {
            persistence.updated(connector);

            Collection<? extends String> inboundModelVersions = connector.getInboundModelVersion();
            ArrayList<String> updatedList = new ArrayList<>(inboundModelVersions);
            updatedList.add("3.0.0");
            connector.setInboundModelVersion(updatedList);

            persistence.updated(connector);
            Assert.assertTrue(repositoryFacade.getConnectorFromTripleStore(URI.create("http://localhost:8080/connectors/" + connector.getId().hashCode())).getInboundModelVersion().contains("3.0.0"));
            //Assert.assertTrue(repositoryFacade.getConnectorFromTripleStore(connector.getId()).getInboundModelVersion().contains("3.0.0"));
        }
        catch (IOException | RejectMessageException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    //TODO: restore test
    @Test
    public void unavailable() {
        long sizeBeforeIngest = repositoryFacade.getSize();

        InfrastructureComponent connector1 = createConnector();
        InfrastructureComponent connector2 = createConnector();
        try {
            //Make it available
            persistence.updated(connector1);
            int sizeGraph1 = repositoryFacade.getSize();
            Assert.assertEquals(sizeGraph1, sizeBeforeIngest + 1);



            persistence.updated(connector2);
            long sizeGraph1And2 = repositoryFacade.getSize();

            //Assert.assertEquals(3, repositoryFacade.getContextIds().size());
            Assert.assertEquals(sizeGraph1And2, sizeGraph1 + 1);

            persistence.unavailable(connector2.getId());
            Assert.assertEquals(repositoryFacade.getSize(), sizeGraph1);
            persistence.unavailable(connector1.getId());
            Assert.assertEquals(repositoryFacade.getSize(), sizeBeforeIngest);
        }
        catch (IOException | RejectMessageException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void query() throws RejectMessageException {
        try {
            persistence.updated(createConnector());
        }
        catch (IOException | RejectMessageException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
        String query = "SELECT * WHERE {?connector a <https://w3id.org/idsa/core/BaseConnector>}";
        String queryResult = persistence.getResults(query);


        //The SPARQL Queries are rewritten by the SparqlQueryRewriter to make sure that the query is evaluated on all active graphs
        //For this, a new variable is introduced, ?__RESERVED, containing the name of the original graph. We should probably rename this
        Assert.assertTrue(queryResult.contains("?__RESERVED"));
        Assert.assertTrue(queryResult.contains("http://"));
    }
}
