package de.fraunhofer.iais.eis.ids.broker.acceptancetest;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.main.AppConfig;
import de.fraunhofer.iais.eis.ids.broker.util.NullBrokerSelfDescription;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.Multipart;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.QueryMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class ConnectorManagementTest {

    private MultipartComponentInteractor multipartComponentInteractor;
    private DynamicAttributeToken dummyToken = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("test1234").build();

    private Message connectorAvailable;

    private Message connectorUnavailable;

    private Message configChange;

    private InfrastructureComponent connector;

    private QueryMessage brokerQuery;

    @Before
    public void setUp() throws URISyntaxException, MalformedURLException {
        multipartComponentInteractor = new AppConfig(new NullBrokerSelfDescription()).catalogUri(URI.create("http://localhost:8080/connectors/")).responseSenderAgent(new URI("http://example.org/agent/")).build();

        connectorAvailable =  new ConnectorUpdateMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_("3.0.0-SNAPSHOT")
                ._issuerConnector_(new URI("http://example.org/connector1"))
                ._affectedConnector_(new URI("http://example.org/connector1"))
                ._securityToken_(dummyToken)
                ._senderAgent_(new URI("http://example.org/agent/"))
                .build();

        connectorUnavailable = new ConnectorUnavailableMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_("3.0.0-SNAPSHOT")
                ._issuerConnector_(new URI("http://example.org/connector1"))
                ._affectedConnector_(new URI("http://example.org/connector1"))
                ._securityToken_(dummyToken)
                ._senderAgent_(new URI("http://example.org/agent/"))
                .build();

        //re-enable
        configChange = new ConnectorUpdateMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_("3.0.0-SNAPSHOT")
                ._issuerConnector_(new URI("http://example.org/connector1"))
                ._affectedConnector_(new URI("http://example.org/connector1"))
                ._securityToken_(dummyToken)
                ._senderAgent_(new URI("http://example.org/agent/"))
                .build();

        connector = new BaseConnectorBuilder(new URI("http://example.org/connector1"))
                ._title_(new ArrayList<>(asList(new TypedLiteral("DWD Open Data Connector", "en"))))
                ._curator_(new URI("http://example.org/participant1"))
                ._maintainer_(new URI("http://example.org/participant1"))
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(asList("3.0.0"))
                ._resourceCatalog_(asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

        brokerQuery = new QueryMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_("3.0.0-SNAPSHOT")
                ._issuerConnector_(new URI("http://example.org/connector1"))
                ._securityToken_(dummyToken)
                ._senderAgent_(new URI("http://example.org/agent/"))
                .build();
    }

    @Test
    public void register() throws JSONException, IOException {
        InfrastructureComponentMAP infrastructureComponentStatusMAP = new InfrastructureComponentMAP(
                connectorAvailable,
                connector);

        String responseMessage = getComponentResponse(infrastructureComponentStatusMAP);
        Assert.assertEquals("ids:MessageProcessedNotificationMessage", new JSONObject(responseMessage).get("@type"));


        // TODO sba: ugly hack:
        update();
        unregister();
    }

    private String getComponentResponse(MessageAndPayload map) throws IOException {
        Multipart registration = new Multipart(map);
        Multipart registrationResponse = multipartComponentInteractor.process(registration, RequestType.INFRASTRUCTURE);

        return registrationResponse.getHeader();
    }


    //@Test
    public void unregister() throws JSONException, IOException {
        InfrastructureComponentMAP infrastructureComponentStatusMAP = new InfrastructureComponentMAP(
                connectorUnavailable,
                connector);

        String responseMessage = getComponentResponse(infrastructureComponentStatusMAP);
        Assert.assertEquals("ids:MessageProcessedNotificationMessage", new JSONObject(responseMessage).get("@type"));
    }

    //@Test
    public void update() throws JSONException, IOException {
        InfrastructureComponentMAP infrastructureComponentStatusMAP = new InfrastructureComponentMAP(
                configChange,
                connector);
        String responseMessage = getComponentResponse(infrastructureComponentStatusMAP);
        Assert.assertEquals("ids:MessageProcessedNotificationMessage", new JSONObject(responseMessage).get("@type"));
    }

    @Test
    @Ignore // TODO: reactivate the test. If the FUSEKI is not available, a RejectionMessage comes back
    public void query() throws IOException, JSONException {
        QueryMAP queryMAP = new QueryMAP(
                brokerQuery,
                "SELECT * WHERE {?connector a <https://w3id.org/idsa/core/BaseConnector>}");
        String responseMessage = getComponentResponse(queryMAP);
        Assert.assertEquals("ids:ResultMessage", new JSONObject(responseMessage).get("@type"));
    }

}
