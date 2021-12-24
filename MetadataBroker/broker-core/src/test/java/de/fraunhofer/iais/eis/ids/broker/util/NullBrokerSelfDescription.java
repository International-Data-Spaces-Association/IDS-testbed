package de.fraunhofer.iais.eis.ids.broker.util;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.index.common.impl.IndexSelfDescription;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class NullBrokerSelfDescription implements SelfDescriptionProvider {

    private final IndexSelfDescription indexSelfDescription;

    public static final String componentId = "http://example.org/ids/broker";
    public static final String maintainerId = "http://example.org/ids/maintainer";
    public static final String catalogId = "http://example.org/ids/broker/catalog";
    public static final String modelVersion = "4.0.0";
    public static final String componentUri = "https://broker.ids.isst.fraunhofer.de/";


    public NullBrokerSelfDescription() throws MalformedURLException, URISyntaxException {
        indexSelfDescription = new IndexSelfDescription(
                URI.create(componentId),
                URI.create(maintainerId),
                URI.create(catalogId),
                modelVersion,
                null,
                URI.create(componentUri));
    }

    public InfrastructureComponent getSelfDescription() {
        return indexSelfDescription.getSelfDescription();
    }

}
