package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.logging.MessageLogger;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.SameOriginResourceMapValidationStrategy;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.ValidatingMessageHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.resource.map.ResourceMAP;
import de.fraunhofer.iais.eis.ids.index.common.persistence.RepositoryFacade;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is a message handler for messages about the status of resources,
 * such as ResourceAvailableMessages, ResourceUpdateMessages, and ResourceUnavailableMessages
 */
public class ResourceMessageHandler extends ValidatingMessageHandler<ResourceMAP, DefaultSuccessMAP> {

    private final ResourceStatusHandler resourceStatusHandler;
    private final InfrastructureComponent infrastructureComponent;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgent;
    private final RepositoryFacade repositoryFacade;

    /**
     * Constructor
     * @param resourceStatusHandler The component which then takes care of persisting the changes
     * @param infrastructureComponent The broker as infrastructure component, such that appropriate responses can be sent
     * @param securityTokenProvider A security token provider for sending responses with a DAT
     * @param responseSenderAgent The "senderAgent" which should show in automatic response messages
     */
    public ResourceMessageHandler(ResourceStatusHandler resourceStatusHandler, InfrastructureComponent infrastructureComponent, SecurityTokenProvider securityTokenProvider, RepositoryFacade repositoryFacade, URI responseSenderAgent)
    {
        this.resourceStatusHandler = resourceStatusHandler;
        this.infrastructureComponent = infrastructureComponent;
        this.addMapValidationStrategy(new SameOriginResourceMapValidationStrategy());
        this.securityTokenProvider = securityTokenProvider;
        this.repositoryFacade = repositoryFacade;
        this.responseSenderAgent = responseSenderAgent;
    }

    /**
     * This function takes care of an inbound message which can be handled by this class
     * @param messageAndPayload The message to be handled
     * @return MessageProcessedNotification wrapped in a DefaultSuccessMAP, if the message has been processed properly
     * @throws RejectMessageException thrown, if the message could not be processed properly
     */
    @Override
    public DefaultSuccessMAP handleValidated(ResourceMAP messageAndPayload) throws RejectMessageException {
        ResourceNotificationMessage msg = (ResourceNotificationMessage) messageAndPayload.getMessage();
        MessageLogger.logMessage(messageAndPayload, true, "affectedResource");
        URI rewrittenUri = null;
        try {
            if (msg instanceof ResourceUpdateMessage) {
                //ResourceUpdateMessages have the affected Resource in their payload
                if (msg.getAffectedResource() != null && messageAndPayload.getPayload().isPresent()) {

                    //TODO: Check if method is POST and, if so, if Resource already exists
                    if(messageAndPayload.getMessage().getProperties() != null) {
                        //POST is not idempotent. Making sure that, in case POST is used, the connector does not exist yet
                        if (messageAndPayload.getMessage().getProperties().containsKey("https://w3id.org/idsa/core/method")) {
                            String method = messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/method").toString().replace("\"", "").replace("^^http://www.w3.org/2001/XMLSchema#string", "").toLowerCase();
                            if (method.equals("post")) {
                                try {
                                    //Check if resource exists yet
                                    if (resourceStatusHandler.resourceExists(((ResourceUpdateMessage) messageAndPayload.getMessage()).getAffectedResource())
                                            || resourceStatusHandler.resourceExists(ResourcePersistenceAndIndexing.tryGetRewrittenResourceUri(messageAndPayload.getMessage().getIssuerConnector(), ((ResourceUpdateMessage) messageAndPayload.getMessage()).getAffectedResource()))) {
                                        throw new RejectMessageException(RejectionReason.TOO_MANY_RESULTS, new Exception("The resource you are trying to post already exists. To update it, use PUT instead."));
                                    }
                                }
                                catch (RejectMessageException ignored)
                                {
                                    //RejectMessageException is thrown by ResourcePersistenceAndIndexing.tryGetRewrittenResourceUri, in case the resource does not exist
                                    //This may very well happen here, particularly if the resources is posted correctly (i.e. didn't exist before)
                                }
                            }
                        }
                    }
                    rewrittenUri = resourceStatusHandler.updated(messageAndPayload.getPayload().get(), msg.getIssuerConnector());
                } else {
                    //If no payload present, Resource cannot be updated
                    throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Affected Resource is null or payload is missing"));
                }
            } else if (msg instanceof ResourceUnavailableMessage) {
                //ResourceUnavailableMessages only contain a reference to the Resource which is now unavailable. Payload should be null
                if (msg.getAffectedResource() != null) {
                    resourceStatusHandler.unavailable(msg.getAffectedResource(), msg.getIssuerConnector());
                } else {
                    throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("Affected Resource is null"));
                }
            }


        } catch (Exception e) {
            //Exception occurred. If it is already a RejectMessageException, throw that. If not, create a RejectMessageException with other RejectionReason
            if (e instanceof RejectMessageException) {
                throw (RejectMessageException) e;
            }
            //For some reason, ConnectExceptions sometimes do not provide an exception message.
            //This causes a NullPointerException and returns an HTTP 500
            e.printStackTrace();
            if (e.getMessage() == null) {
                e = new Exception(e.getClass().getName() + " with empty message.");
            }
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
        try {
            //No Exception occurred. Send MessageProcessedNotificationMessage
            DefaultSuccessMAP returnValue = new DefaultSuccessMAP(infrastructureComponent.getId(), infrastructureComponent.getOutboundModelVersion(), messageAndPayload.getMessage().getId(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
            if(rewrittenUri != null)
            {
                //Attach the rewritten URI to the response, so that the recipient knows under which address the resource can be found
                returnValue.getMessage().setProperty("Location", "<" + rewrittenUri.toString() + ">");
            }
            return returnValue;
        }
        catch (TokenRetrievalException e)
        {
            //Could not retrieve own DAT. Outgoing message cannot be validated by other connector. Sending an error message
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * This function provides a list of message types which are supported by this class
     * @return List of supported message types
     */
    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Arrays.asList(ResourceUpdateMessage.class, ResourceUnavailableMessage.class);
    }
}
