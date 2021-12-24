package de.fraunhofer.iais.eis.ids.broker.main;

import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ConnectorUnavailableValidationStrategy;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.RegistrationHandler;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourceMessageHandler;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourcePersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.SelfDescriptionPersistenceAndIndexing;
import de.fraunhofer.iais.eis.ids.component.core.DefaultComponent;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.DapsSecurityTokenVerifier;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.JWKSFromIssuer;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.QueryHandler;
import de.fraunhofer.iais.eis.ids.index.common.main.AppConfigTemplate;
import de.fraunhofer.iais.eis.ids.index.common.persistence.ConstructQueryResultHandler;
import de.fraunhofer.iais.eis.ids.index.common.persistence.DescriptionProvider;
import de.fraunhofer.iais.eis.ids.index.common.persistence.DescriptionRequestHandler;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class is used to start up a broker with appropriate settings and is only created once from the Main class
 */
public class AppConfig extends AppConfigTemplate {

    private final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     * Class constructor
     * @param selfDescriptionProvider SelfDescriptionProvider object from which information can be obtained which is required for providing self-descriptions
     */
    public AppConfig(SelfDescriptionProvider selfDescriptionProvider) {
        super(selfDescriptionProvider);
    }

    /**
     * Build method to be called once all configuration has been finished
     * @return MultipartComponentInteractor object, which is configured according to this AppConfig instance
     */
    @Override
    public MultipartComponentInteractor build() {
        //Try to pre-initialize the SHACL validation shapes so that this won't slow us down during message handling
        //TODO: Do this in a separate thread
        if(performShaclValidation) {
            try {
                ShaclValidator.initialize();
            } catch (IOException e) {
                logger.warn("Failed to initialize Shapes for SHACL validation.", e);
            }
        }

        //Repository facade is our bridge to the triple store backend
        RepositoryFacade repositoryFacade = new RepositoryFacade(sparqlEndpointUrl);

        //Object taking care of storing connectors and their resources in a triple store with optional indexing
        SelfDescriptionPersistenceAndIndexing selfDescriptionPersistence = new SelfDescriptionPersistenceAndIndexing(
                repositoryFacade, catalogUri, indexing);

        //Object taking care of modifications to resources, such as connectors registering new resources
        //In contrast to the SelfDescriptionPersistenceAndIndexing, this class takes care of persisting resources coming from ResourceUpdateMessages and ResourceUnavailableMessages
        ResourcePersistenceAndIndexing resourcePersistenceAndIndexing = new ResourcePersistenceAndIndexing(
                repositoryFacade, catalogUri);
        resourcePersistenceAndIndexing.setIndexing(indexing);

        //Strategy for fetching the context for JSON-LD objects
        if (contextDocumentUrl != null && !contextDocumentUrl.isEmpty()) {
            selfDescriptionPersistence.setContextDocumentUrl(contextDocumentUrl);
            resourcePersistenceAndIndexing.setContextDocumentUrl(contextDocumentUrl);
            ConstructQueryResultHandler.contextDocumentUrl = contextDocumentUrl;
        }
        ConstructQueryResultHandler.catalogUri = (catalogUri == null) ? new ResourceCatalogBuilder().build().getId().toString() : catalogUri.toString();

        //Message handler for ConnectorUpdateMessages and ConnectorUnavailableMessages
        RegistrationHandler registrationHandler = new RegistrationHandler(selfDescriptionPersistence, selfDescriptionProvider.getSelfDescription(), securityTokenProvider, repositoryFacade, responseSenderAgent);
        //Add some security checks, e.g. preventing signing off of other connectors
        registrationHandler.addMapValidationStrategy(new ConnectorUnavailableValidationStrategy(repositoryFacade));
        //Message handler for QueryMessages
        QueryHandler queryHandler = new QueryHandler(selfDescriptionProvider.getSelfDescription(), selfDescriptionPersistence, securityTokenProvider, responseSenderAgent);

        //Message handler for ResourceUpdateMessages and ResourceUnavailableMessages
        ResourceMessageHandler resourceHandler = new ResourceMessageHandler(resourcePersistenceAndIndexing, selfDescriptionProvider.getSelfDescription(), securityTokenProvider, repositoryFacade, responseSenderAgent);

        //Component object required for interactions
        DefaultComponent component = new DefaultComponent(selfDescriptionProvider, securityTokenProvider, responseSenderAgent, false);

        //Class to provide descriptions for objects persisted in our triple store, as well as providing self-descriptions
        DescriptionProvider descriptionProvider = new DescriptionProvider(selfDescriptionProvider.getSelfDescription(), repositoryFacade, catalogUri);
        //The corresponding message handler
        DescriptionRequestHandler descriptionHandler = new DescriptionRequestHandler(descriptionProvider, securityTokenProvider, responseSenderAgent);
        component.addMessageHandler(descriptionHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(registrationHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(queryHandler, RequestType.INFRASTRUCTURE);
        component.addMessageHandler(resourceHandler, RequestType.INFRASTRUCTURE);

        if (dapsValidateIncoming) {
            component.setSecurityTokenVerifier(new DapsSecurityTokenVerifier(new JWKSFromIssuer(trustedJwksHosts)));
        }

        //Wrap everything up in a single object and return
        return new MultipartComponentInteractor(component, securityTokenProvider, responseSenderAgent, performShaclValidation);
    }

}
