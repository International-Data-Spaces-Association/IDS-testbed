package de.fraunhofer.iais.eis.ids.broker.main;


import de.fraunhofer.iais.eis.ids.component.core.InfomodelFormalException;
import de.fraunhofer.iais.eis.ids.component.protocol.http.server.ComponentInteractorProvider;
import de.fraunhofer.iais.eis.ids.index.common.main.MainTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;

/**
 * Entry point to the Broker
 */
@Configuration
@EnableAutoConfiguration(exclude = SolrAutoConfiguration.class)
@ComponentScan(basePackages = { "de.fraunhofer.iais.eis.ids.component.protocol.http.server"} )
public class Main extends MainTemplate implements ComponentInteractorProvider {

    Logger logger = LoggerFactory.getLogger(Main.class);

    //Initializing properties which are not inherited from MainTemplate
    @Value("${sparql.url}")
    private String sparqlEndpointUrl;

    @Value("${infomodel.contextUrl}")
    private String contextDocumentUrl;

    @Value("${jwks.trustedHosts}")
    private Collection<String> trustedJwksHosts;

    @Value("${daps.validateIncoming}")
    private boolean dapsValidateIncoming;

    @Value("${component.responseSenderAgent}")
    private String responseSenderAgent;

    @Value("${infomodel.validateWithShacl}")
    private boolean validateShacl;

    //Environment allows us to access application.properties
    @Autowired
    private Environment env;

    /**
     * This function is called during startup and takes care of the initialization
     */
    @PostConstruct
    @Override
    public void setUp() {
        //Assigning variables which are inherited from MainTemplate
        componentUri = env.getProperty("component.uri");
        componentMaintainer = env.getProperty("component.maintainer");
        componentCatalogUri = env.getProperty("component.catalogUri");
        componentModelVersion = env.getProperty("component.modelversion");
        sslCertificatePath = env.getProperty("ssl.certificatePath");
        elasticsearchHostname = env.getProperty("elasticsearch.hostname");
        elasticsearchPort = Integer.parseInt(Objects.requireNonNull(env.getProperty("elasticsearch.port")));
        keystorePassword = env.getProperty("keystore.password");
        keystoreAlias = env.getProperty("keystore.alias");
//        componentIdsId = env.getProperty("component.idsid");
        dapsUrl = env.getProperty("daps.url");
        trustAllCerts = Boolean.parseBoolean(env.getProperty("ssl.trustAllCerts"));
        ignoreHostName = Boolean.parseBoolean(env.getProperty("ssl.ignoreHostName"));

        try {
            multipartComponentInteractor = new AppConfig(createSelfDescriptionProvider())
                    .sparqlEndpointUrl(sparqlEndpointUrl)
                    .contextDocumentUrl(contextDocumentUrl)
                    .catalogUri(new URI(componentCatalogUri))
                    .securityTokenProvider(createSecurityTokenProvider())
                    .trustedJwksHosts(trustedJwksHosts)
                    .dapsValidateIncoming(dapsValidateIncoming)
                    .responseSenderAgent(new URI(responseSenderAgent))
                    .performShaclValidation(validateShacl)
                    .build();
        }
        catch (URISyntaxException e) {
            throw new InfomodelFormalException(e);
        }
    }

    @Override @PreDestroy
    public void shutDown() throws IOException {

    }


    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
