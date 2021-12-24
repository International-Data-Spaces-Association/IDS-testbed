package de.fraunhofer.iais.eis.ids.index.common.impl;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;

import static de.fraunhofer.iais.eis.util.Util.asList;

/**
 * This class is used to create a self description document for an IDS metadata broker
 * The description contains information about the operator, security related information and a reference to the catalog
 * Note that the self description does NOT contain the full catalog, as this would cause too much unnecessary traffic. If the catalog is required, it can be requested explicitly
 */
public class IndexSelfDescription implements SelfDescriptionProvider {
    public final URI componentId, maintainerId, catalogUri, componentUri;
    public final String modelVersion;
    public String sslCertificatePath;
    public final Logger logger = LoggerFactory.getLogger(IndexSelfDescription.class);

    /**
     * This deprecated constructor does not allow to add the SHA256 fingerprint of the TLS certificate for the HTTPS connection to the self description
     * We might un-deprecate this, if it turns out that the fingerprint is not required in the self description
     */
    @Deprecated
    public IndexSelfDescription(URI componentId, URI maintainerId, URI catalogUri, String modelVersion) {
        this.componentId = componentId;
        this.maintainerId = maintainerId;
        this.catalogUri = catalogUri;
        this.modelVersion = modelVersion;
        this.componentUri = URI.create("http://example.org/some/deployed/connector/URL");
    }

    /**
     * Constructor
     * @param componentId The URI of this broker
     * @param maintainerId The URI of the maintainer for this broker
     * @param catalogUri The URI of the broker's catalog. This needs to be provided explicitly so that users can request the full catalog
     * @param modelVersion The IDS information model version which is used by this broker
     * @param sslCertificatePath Path to the TLS certificate which is used for the HTTPS connection
     * @param componentUri The URI this broker is hosted at
     */
    public IndexSelfDescription(URI componentId, URI maintainerId, URI catalogUri, String modelVersion, String sslCertificatePath, URI componentUri) {
        this.componentId = componentId;
        this.maintainerId = maintainerId;
        this.catalogUri = catalogUri;
        this.modelVersion = modelVersion;
        this.sslCertificatePath = sslCertificatePath;
        this.componentUri = componentUri;
    }

    /**
     * This function generates a self description document from the arguments passed to the constructor
     * @return An IDS InfrastructureComponent which can be turned into a self description document by calling .toRdf() on it
     */
    @Override
    public InfrastructureComponent getSelfDescription() {
        ResourceCatalog catalog = new ResourceCatalogBuilder()
                ._offeredResource_(createResourcesList())
                .build();

        //Host host = new HostBuilder()._protocol_(Protocol.HTTP)._accessUrl_(componentUri).build();

        //To avoid having double slashes at some places, make sure that we have no trailing slash before appending things like "/infrastructure"
        URI componentUriWithoutTrailingSlash = componentUri;
        if(componentUri.toString().endsWith("/"))
        {
            componentUriWithoutTrailingSlash = URI.create(componentUri.toString().substring(0, componentUri.toString().length() - 1));
        }

        BrokerBuilder builder = new BrokerBuilder(componentId)
                ._title_(asList(new TypedLiteral("IDS Metadata Broker", "en")))
                ._description_(asList(new TypedLiteral("A Broker with a graph persistence layer@en")))
                ._maintainer_(maintainerId)
                ._curator_(maintainerId)
                ._inboundModelVersion_(Util.asList(modelVersion))
                ._outboundModelVersion_(modelVersion)
                ._resourceCatalog_(Util.asList(catalog))
                ._hasEndpoint_(asList(new ConnectorEndpointBuilder()
                        ._path_("/infrastructure")
                        ._endpointDocumentation_(Util.asList(URI.create("https://app.swaggerhub.com/apis/idsa/IDS-Broker/1.3.1#/Multipart%20Interactions/post_infrastructure")))
                        ._accessURL_(URI.create(componentUriWithoutTrailingSlash + "/infrastructure"))
                        ._endpointInformation_(asList(
                                new TypedLiteral("This endpoint provides IDS Connector and IDS Resource registration and search capabilities at the IDS Metadata Broker.","en"),
                                new TypedLiteral("Dieser Endpunkt ermöglicht die Registrierung von und das Suchen nach IDS Connectoren und IDS Ressourcen am IDS Metadata Broker.", "de")
                        ))
                        .build()))

                // Register Connector

                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()
                        ._path_("/")
                        ._endpointInformation_(Util.asList(
                                new TypedLiteral("Endpoint providing a self-description of this connector", "en"),
                                new TypedLiteral("Dieser Endpunkt liefert eine Selbstbeschreibung dieses IDS Connectors", "de")))
                        ._accessURL_(URI.create(componentUriWithoutTrailingSlash + "/"))
                        .build())
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE);
        return builder.build();
    }

    /**
     * This function creates an empty catalog showing one dummy entry with the catalog ID. This helps connectors to retrieve the catalog URI and allows to request the full catalog
     * @return Dummy list of resources
     */
    private ArrayList<Resource> createResourcesList() {
        Artifact catalog = new ArtifactBuilder(catalogUri).build();
        Representation catalogRepresentation = new DataRepresentationBuilder()
                ._instance_(asList(catalog))
                .build();
        DataResource resource = new DataResourceBuilder()
                ._representation_(asList(catalogRepresentation))
                .build();
        return asList(resource);
    }

}
