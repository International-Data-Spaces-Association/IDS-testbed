package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;


import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationStrategy;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * This class provides validation mechanisms that the sender of a ConnectorUnavailableMessage is authorized to de-register the connector in question
 * @author maboeckmann
 */
public class ConnectorUnavailableValidationStrategy implements MapValidationStrategy<InfrastructureComponentMAP> {

    //Repository facade allows access to the triple store to look up values of connector to be altered
    private final RepositoryFacade repositoryFacade;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Constructor
     *
     * @param repositoryFacade providing access to the triple store backend
     */
    public ConnectorUnavailableValidationStrategy(RepositoryFacade repositoryFacade) {
        this.repositoryFacade = repositoryFacade;
    }

    @Override
    public void validate(InfrastructureComponentMAP map) throws MapValidationException {

        /*
         * De-registration request is successful only if (1) the connector unregisters itself or (2) the request originates
         * from a connector that is already registered at the broker AND has the same maintainer participant as the connector
         * that should be unregistered
         */


        Message msg = map.getMessage();
        if (msg instanceof ConnectorUnavailableMessage) {
            //URIs of registered connectors are rewritten. Need to make sure to rewrite this URI in the same way
            URI issuer = SelfDescriptionPersistenceAndIndexing.rewriteConnectorUri(msg.getIssuerConnector());

            //Usually, the URI of the affected connector SHOULD already have been rewritten. For ease of use, we cover both cases here
            URI affected = ((ConnectorUnavailableMessage)msg).getAffectedConnector();

            //Check whether affectedConnector has been rewritten already by the client. Rewriting an already rewritten URI will result in a different (wrong) URI
            if(!affected.getHost().equals(issuer.getHost()))
            {
                //It has not been rewritten
                affected = SelfDescriptionPersistenceAndIndexing.rewriteConnectorUri(affected);
            }

            logger.debug("Signing off connector. Validating legitimacy. Issuer: " + issuer + " - affected: " + affected);

            //Self-de-registration?
            if (!issuer.equals(affected)) {
                //No self-de-registration. Check for maintainer
                try {
                    //Try to look up the issuing connector to grab its maintainer
                    Connector issuerConnector = repositoryFacade.getConnectorFromTripleStore(issuer);
                    //Also try to look up the connector to be de-registered
                    Connector affectedConnector = repositoryFacade.getConnectorFromTripleStore(affected);
                    //This is a legal query, if the maintainers match
                    if (!issuerConnector.getMaintainer().equals(affectedConnector.getMaintainer())) {
                        //No match
                        throw new MapValidationException("You may not sign off a foreign connector");
                    }
                } catch (RejectMessageException e) //thrown if Connector could not be found
                {
                    throw new MapValidationException("You may not sign off a foreign connector");
                }
            }

            logger.debug("ConnectorUnavailableValidationStrategy passed.");
        }
    }
}
