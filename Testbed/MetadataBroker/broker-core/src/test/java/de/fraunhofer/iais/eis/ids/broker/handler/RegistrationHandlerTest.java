package de.fraunhofer.iais.eis.ids.broker.handler;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.RegistrationHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.InfrastructureComponentStatusHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class RegistrationHandlerTest {

    private final URI dummyUri = new URI("https://example.org/dummy");
    private final InfrastructureComponent connector = new BaseConnectorBuilder(dummyUri)
            ._title_(new ArrayList<>(asList(new TypedLiteral("DWD Open Data Connector", "en"))))
            ._curator_(dummyUri)
            ._maintainer_(dummyUri)
            ._outboundModelVersion_("3.0.0")
            ._inboundModelVersion_(asList("3.0.0"))
            ._resourceCatalog_(asList(new ResourceCatalogBuilder().build()))
            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
            ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
            .build();
    private final SecurityTokenProvider securityTokenProvider = new SecurityTokenProvider() {
        @Override
        public String getSecurityToken() {
            return "test1234";
        }
    };
    private final URI senderAgent = new URI("http://example.org/");

    private final InfrastructureComponent broker = new BrokerBuilder()
            ._title_(asList(new TypedLiteral("EIS Broker", "en")))
            ._description_(asList(new TypedLiteral("A semantic impl for demonstration purposes", "en")))
            ._maintainer_(dummyUri)
            ._curator_(dummyUri)
            ._inboundModelVersion_(Util.asList("3.0.0"))
            ._outboundModelVersion_("3.0.0")
            ._resourceCatalog_(asList(new ResourceCatalogBuilder().build()))
            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
            ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
            .build();

    private final Message connectorAvailable = new ConnectorUpdateMessageBuilder()
            ._issued_(CalendarUtil.now())
            ._modelVersion_("3.0.0")
            ._issuerConnector_(dummyUri)
            ._affectedConnector_(dummyUri)
            ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
            ._senderAgent_(senderAgent)
            .build();

    private final Message connectorUnavailable = new ConnectorUnavailableMessageBuilder()
            ._issued_(CalendarUtil.now())
            ._modelVersion_("3.0.0")
            ._issuerConnector_(connector.getId())
            ._affectedConnector_(connector.getId())
            ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
            ._senderAgent_(senderAgent)
            .build();

    public RegistrationHandlerTest() throws TokenRetrievalException, URISyntaxException {
    }

    @Test
    public void handleRegister() throws RejectMessageException, URISyntaxException {
        RegistrationHandler registrationHandler = new RegistrationHandler(new InfrastructureComponentStatusHandler() {

            @Override
            public void unavailable(URI issuerConnector) {
                Assert.fail();
            }

            @Override
            public URI updated(InfrastructureComponent selfDescription) {
                Assert.assertEquals(connector, selfDescription);
                return null;
            }

        }, broker, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return "test1234";
            }
        }, new RepositoryFacade(), new URI("http://example.org/"));
        registrationHandler.handle(new InfrastructureComponentMAP(connectorAvailable, connector));
    }

    @Test
    public void handleUnregister() throws RejectMessageException, URISyntaxException {
        RegistrationHandler registrationHandler = new RegistrationHandler(new InfrastructureComponentStatusHandler() {

            @Override
            public void unavailable(URI issuerConnector) {
                Assert.assertEquals(issuerConnector, connectorUnavailable.getIssuerConnector());
            }

            @Override
            public URI updated(InfrastructureComponent selfDescription) {
                Assert.fail();
                return null;
            }

        }, broker, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return "test1234";
            }
        }, new RepositoryFacade(), new URI("http://example.org/"));

        registrationHandler.handle(new InfrastructureComponentMAP(connectorUnavailable));
    }


    @Test
    public void handleUpdate() throws RejectMessageException, URISyntaxException {
        RegistrationHandler registrationHandler = new RegistrationHandler(new InfrastructureComponentStatusHandler() {

            @Override
            public void unavailable(URI issuerConnector) {
                Assert.fail();
            }

            @Override
            public URI updated(InfrastructureComponent selfDescription) {
                Assert.assertEquals(connector, selfDescription);
                return null;
            }

        }, broker, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return "test1234";
            }
        }, new RepositoryFacade(), new URI("http://example.org/"));

        registrationHandler.handle(new InfrastructureComponentMAP(connectorAvailable, connector));
    }

    //TODO: Add test for passivation!


}
