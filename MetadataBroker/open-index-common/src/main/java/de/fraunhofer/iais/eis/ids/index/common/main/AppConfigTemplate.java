package de.fraunhofer.iais.eis.ids.index.common.main;

import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.SelfDescriptionProvider;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.index.common.persistence.NullIndexing;
import de.fraunhofer.iais.eis.ids.index.common.persistence.spi.Indexing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * Template class for AppConfig of an index service.
 * This template should be used to reduce code redundancy and to improve maintainability of various index services
 */
public abstract class AppConfigTemplate {

    final private Logger logger = LoggerFactory.getLogger(AppConfigTemplate.class);

    public String sparqlEndpointUrl = "";
    public String contextDocumentUrl;
    public URI catalogUri;
    public SelfDescriptionProvider selfDescriptionProvider;

    //Try to find some indexing on classpath. If not present, use Null Indexing
    public Indexing indexing = ServiceLoader.load(Indexing.class).findFirst().orElse(new NullIndexing<>());
    public SecurityTokenProvider securityTokenProvider = new SecurityTokenProvider() {
        @Override
        public String getSecurityToken() {
            return "";
        }
    };

    /**
     * This function can be used to overwrite the default behaviour of trying to find any indexing in the classpath
     * @param indexing Desired indexing implementation to be used
     * @return AppConfigTemplate with new value set for indexing
     */
    public AppConfigTemplate setIndexing(Indexing indexing)
    {
        logger.info("Setting indexing to " + indexing.getClass().getSimpleName());
        this.indexing = indexing;
        return this;
    }

    public Collection<String> trustedJwksHosts;
    public boolean dapsValidateIncoming;
    public URI responseSenderAgent;
    public boolean performShaclValidation;

    /**
     * Constructor
     * @param selfDescriptionProvider Object providing a self-description of this indexing service
     */
    public AppConfigTemplate(SelfDescriptionProvider selfDescriptionProvider) {
        this.selfDescriptionProvider = selfDescriptionProvider;
    }

    /**
     * Call this function to set the URL of the SPARQL endpoint. If none is set, an in-memory solution will be used, not permanently persisting data.
     * @param sparqlEndpointUrl Address of the SPARQL endpoint as String
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate sparqlEndpointUrl(String sparqlEndpointUrl) {
        this.sparqlEndpointUrl = sparqlEndpointUrl;
        logger.info("SPARQL endpoint set to " +sparqlEndpointUrl);
        return this;
    }


    /**
     * URL of the context document to be used for JSON-LD context
     * @param contextDocumentUrl URL of the context document
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate contextDocumentUrl(String contextDocumentUrl) {
        this.contextDocumentUrl = contextDocumentUrl;
        logger.info("Context document URL set to " +contextDocumentUrl);
        return this;
    }

    /**
     * This function allows to set the URI of the catalog of this indexing service. This is required for it to know when the catalog is requested, and to rewrite URIs for the REST scheme
     * @param catalogUri URI of the own catalog.
     * @return AppConfig as Builder Object
     * TODO: Theoretically, this could be a list of URIs in case of multiple Catalogs. Possibly useful to distinguish between ResourceCatalogUri and ConnectorCatalogUri
     */
    public AppConfigTemplate catalogUri(URI catalogUri) {
        this.catalogUri = catalogUri;
        logger.info("Catalog URI set to " + catalogUri.toString());
        return this;
    }

    /**
     * Use this function to turn SHACL validation on or off (configurable at startup only)
     * @param performValidation boolean, indicating whether SHACL validation should be performed
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate performShaclValidation(boolean performValidation)
    {
        this.performShaclValidation = performValidation;
        logger.info("Perform SHACL Validation is set to " + performValidation);
        return this;
    }

    /**
     * Use this function to set a SecurityTokenProvider for this indexing service, allowing it to send messages with a Dynamic Attribute Token signed by the DAPS
     * @param securityTokenProvider Object
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate securityTokenProvider(SecurityTokenProvider securityTokenProvider) {
        this.securityTokenProvider = securityTokenProvider;
        return this;
    }

    /**
     * List of hosts whose signature we can trust. Used by DAT validation
     * @param trustedJwksHosts list of hosts
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate trustedJwksHosts(Collection<String> trustedJwksHosts) {
        this.trustedJwksHosts = trustedJwksHosts;
        return this;
    }

    /**
     * Sets the senderAgent property in all response messages
     * @param responseSenderAgent URI of the senderAgent to be used
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate responseSenderAgent(URI responseSenderAgent) {
        this.responseSenderAgent = responseSenderAgent;
        return this;
    }

    /**
     * Function to toggle validating incoming messages for having a correct DAT. This should ALWAYS be turned on. Only turn this off if required for debugging!
     * @param dapsValidateIncoming boolean, determining whether messages should be checked for having valid security tokens
     * @return AppConfig as Builder Object
     */
    public AppConfigTemplate dapsValidateIncoming(boolean dapsValidateIncoming) {
        this.dapsValidateIncoming = dapsValidateIncoming;
        logger.info("Incoming messages DAPS token verification enabled: " +dapsValidateIncoming);
        return this;
    }

    /**
     * Build function, turning Builder Object into an actual MultipartComponentInteractor
     * @return MultipartComponentInteractor with previously set settings
     */
    public abstract MultipartComponentInteractor build();


}
