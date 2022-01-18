package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.logging.MessageLogger;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.InfrastructureComponentStatusHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.SameOriginInfrastructureComponentMapValidationStrategy;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.ValidatingMessageHandler;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is a message handler for messages about the status of infrastructure components,
 * such as ConnectorAvailableMessages, ConnectorUpdateMessages, ConnectorInactiveMessages, and ConnectorUnavailableMessages
 */
public class RegistrationHandler extends ValidatingMessageHandler<InfrastructureComponentMAP, DefaultSuccessMAP> {

    private final InfrastructureComponent infrastructureComponent;
    private final InfrastructureComponentStatusHandler infrastructureComponentStatusHandler;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderUri;
    private final RepositoryFacade repositoryFacade;

    Logger logger = LoggerFactory.getLogger(RegistrationHandler.class);

    /**
     * Constructor
     * @param infrastructureComponentStatusHandler The component which then takes care of persisting the changes
     * @param infrastructureComponent The broker as infrastructure component, such that appropriate responses can be sent
     * @param securityTokenProvider A security token provider for sending responses with a DAT
     * @param responseSenderUri The "senderAgent" which should show in automatic response messages
     */
    public RegistrationHandler(InfrastructureComponentStatusHandler infrastructureComponentStatusHandler,
                               InfrastructureComponent infrastructureComponent,
                               SecurityTokenProvider securityTokenProvider,
                               RepositoryFacade repositoryFacade,
                               URI responseSenderUri)
    {
        this.infrastructureComponent = infrastructureComponent;
        this.infrastructureComponentStatusHandler = infrastructureComponentStatusHandler;
        this.securityTokenProvider = securityTokenProvider;
        this.repositoryFacade = repositoryFacade;
        this.responseSenderUri = responseSenderUri;

        addMapValidationStrategy(new SameOriginInfrastructureComponentMapValidationStrategy());
    }

    /**
     * This function takes care of an inbound message which can be handled by this class
     * @param messageAndPayload The message to be handled
     * @return MessageProcessedNotification wrapped in a DefaultSuccessMAP, if the message has been processed properly
     * @throws RejectMessageException thrown, if the message could not be processed properly
     */
    @Override
    public DefaultSuccessMAP handleValidated(InfrastructureComponentMAP messageAndPayload) throws RejectMessageException {
        ConnectorNotificationMessage msg = (ConnectorNotificationMessage) messageAndPayload.getMessage();

        //Logs the message (depending on settings not the entire message) including the affectedConnector attribute
        MessageLogger.logMessage(messageAndPayload, true, "affectedConnector");
        URI rewrittenUri = null;
        try {
            //Message is either ConnectorUpdateMessage or ConnectorUnavailableMessage
            if (msg instanceof ConnectorUpdateMessage) {
                //Updating existing Connector or registering new Connector
                if(messageAndPayload.getPayload().isPresent()) {
                    if(messageAndPayload.getMessage().getProperties() != null) {
                        //POST is not idempotent. Making sure that, in case POST is used, the connector does not exist yet
                        if (messageAndPayload.getMessage().getProperties().containsKey("https://w3id.org/idsa/core/method")) {
                            String method = messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/method").toString().replace("\"", "").replace("^^http://www.w3.org/2001/XMLSchema#string", "").toLowerCase();
                            if(method.equals("post"))
                            {
                                //Check if either the provided URL or the rewritten provided URL already exists (and is non-passivated) as graph in our triple store
                                if(repositoryFacade.graphIsActive(SelfDescriptionPersistenceAndIndexing.rewriteConnectorUri(((ConnectorUpdateMessage) messageAndPayload.getMessage()).getAffectedConnector()).toString())
                                || repositoryFacade.graphIsActive(((ConnectorUpdateMessage) messageAndPayload.getMessage()).getAffectedConnector().toString()))
                                {
                                    //TOO_MANY_RESULTS is not exactly an ideal term... No better choice available though
                                    throw new RejectMessageException(RejectionReason.TOO_MANY_RESULTS, new Exception("The connector you are posting already exists. Use PUT instead to overwrite it."));
                                }
                            }
                        }
                    }
                    //Rewrite URL to match our REST scheme
                    rewrittenUri = infrastructureComponentStatusHandler.updated(messageAndPayload.getPayload().get());
                }
                else
                {
                    throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("Missing Payload in ConnectorUpdateMessage"));
                }
            }
            else if (msg instanceof ConnectorUnavailableMessage) {
                //To unregister, no payload is required
                infrastructureComponentStatusHandler.unavailable(messageAndPayload.getMessage().getIssuerConnector());
            }
        }
        catch (Exception e) {
            if(e instanceof RejectMessageException)
            {
                //If it already is a RejectMessageException, throw it as-is
                throw (RejectMessageException) e;
            }
            //For some reason, ConnectExceptions sometimes do not provide an exception message.
            //This causes a NullPointerException and returns an HTTP 500
            logger.error("Failed to process message.", e);
            if(e.getMessage() == null)
            {
                try {
                    e = new Exception(e.getCause().getMessage());
                }
                catch (NullPointerException ignored)
                {
                    e = new Exception(e.getClass().getName() + " with empty message.");
                }
            }
            //Some unknown error has occurred, returning an internal error
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }

        try {
            //Let the connector know that the update was successful
            DefaultSuccessMAP returnValue = new DefaultSuccessMAP(infrastructureComponent.getId(),
                    infrastructureComponent.getOutboundModelVersion(),
                    messageAndPayload.getMessage().getId(),
                    securityTokenProvider.getSecurityTokenAsDAT(),
                    responseSenderUri);
            if(rewrittenUri != null)
            {
                //Attach the rewritten URI to the response, so that the recipient knows under which address the resource can be found
                returnValue.getMessage().setProperty("Location", "<" + rewrittenUri.toString() + ">");
            }
            return returnValue;
        }
        //Thrown in case the broker is unable to obtain its own security token from the DAPS
        catch (TokenRetrievalException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * This function provides a list of message types which are supported by this class
     * @return List of supported message types
     */
    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Arrays.asList(
                ConnectorUnavailableMessage.class,
                ConnectorUpdateMessage.class);
    }
}
