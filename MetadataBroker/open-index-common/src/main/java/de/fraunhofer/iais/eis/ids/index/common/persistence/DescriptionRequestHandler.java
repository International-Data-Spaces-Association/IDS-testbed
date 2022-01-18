package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.logging.MessageLogger;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionRequestMAP;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionResponseMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Message handler class, forwarding the request to the DescriptionProvider and responding with a DescriptionResponseMessage
 * @author maboeckmann
 */
public class DescriptionRequestHandler implements MessageHandler<DescriptionRequestMAP, DescriptionResponseMAP> {

    private final Logger logger = LoggerFactory.getLogger(DescriptionRequestHandler.class);
    private final DescriptionProvider descriptionProvider;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgentUri;
    public static int maxDepth = 10;

    /**
     * Constructor
     * @param descriptionProvider Instance of DescriptionProvider class to retrieve descriptions of the requested objects from triple store
     * @param securityTokenProvider Instance of SecurityTokenProvider to retrieve a DAT for the response messages
     * @param responseSenderAgentUri The ids:senderAgent which should be provided in response messages
     */
    public DescriptionRequestHandler(DescriptionProvider descriptionProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderAgentUri) {
        this.descriptionProvider = descriptionProvider;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgentUri = responseSenderAgentUri;
    }

    /**
     * The actual handle function which is called from the components, if the incoming request can be handled by this class
     * @param messageAndPayload The incoming request
     * @return A DescriptionResponseMessage, if the request could be handled successfully
     * @throws RejectMessageException thrown if an error occurs during the retrieval process, e.g. if the requested object could not be found
     */
    @Override
    public DescriptionResponseMAP handle(DescriptionRequestMAP messageAndPayload) throws RejectMessageException {
        String payload;

        //This is against the recommendations of the LDP specification (recommends to default to Turtle), but is the expected behaviour in IDS
        Lang outputLanguage = RDFLanguages.JSONLD;
        int depth = 0;

        //Log inbound message
        MessageLogger.logMessage(messageAndPayload.getMessage(), "requestedElement");

        //Can we come up with a neater way than using a hardcoded URI? This is a custom header not defined elsewhere
        //Depth determines whether we should only return information about this object, or also about child objects up to a certain hop limit
        if(messageAndPayload.getMessage().getProperties() != null)
        {
            //Content negotiation
            if(messageAndPayload.getMessage().getProperties().containsKey("https://w3id.org/idsa/core/accept"))
            {
                //Try to retrieve Accept headers from request
                ArrayList<String> acceptStrings = new ArrayList<>();
                Object acceptObject = messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/accept");

                //The value should be a comma separated string, wrapped by Jena in a TypedLiteral
                if(TypedLiteral.class.isAssignableFrom(acceptObject.getClass())) //single Accept header?
                {
                    //Throw away datatype definition of variable
                    String acceptable = acceptObject.toString().replace("\"", "").replace("^^http://www.w3.org/2001/XMLSchema#string", "");
                    if(acceptable.contains(",")) //Multiple values?
                    {
                        acceptStrings.addAll(Arrays.asList(acceptable.split(",")));
                    }
                    else
                    { //singular value
                        acceptStrings.add(acceptable);
                    }
                }
                else { //For some reason, sometimes Jena presented this as an ArrayList of TypedLiteral instead. Handle this case here
                    ArrayList<TypedLiteral> acceptTypedLiterals = (ArrayList<TypedLiteral>) messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/accept");
                    acceptStrings = acceptTypedLiterals.stream().map(TypedLiteral::toString).map(s -> s.replace("\"", "").replace("^^http://www.w3.org/2001/XMLSchema#string", "")).collect(Collectors.toCollection(ArrayList::new));
                }
                if(acceptStrings.contains("text/turtle")) //If Turtle is among the Accept Headers (with high priority), we MUST return that, according to LDP specification
                {
                    outputLanguage = RDFLanguages.TURTLE;
                } else if (acceptStrings.contains("application/ld+json")) {
                    outputLanguage = RDFLanguages.JSONLD; //Stick with the default value, if Turtle was not explicitly requested (2nd priority)
                }
                else if(acceptStrings.contains("application/n-triples")) //Otherwise check for n-triples (3rd priority)
                {
                    outputLanguage = RDFLanguages.NTRIPLES;
                }
                else if(acceptStrings.contains("application/rdf+xml")) //4th priority is RDF XML
                {
                    outputLanguage = RDFLanguages.RDFXML;
                }
            }
            //Check if we need to provide also information about child nodes of the requested element
            if(messageAndPayload.getMessage().getProperties().containsKey("https://w3id.org/idsa/core/depth")) {
                //0 is the default value, meaning absolutely no child objects are expanded. Only their URI is shown, which one can dereference to obtain further information
                try {
                    //Check out whether a custom depth is provided (in valid format)
                    String propertyValue = messageAndPayload.getMessage().getProperties().get("https://w3id.org/idsa/core/depth").toString();
                    if (propertyValue.contains("^^")) //expecting something like: 0^^xsd:integer
                    {
                        //Only take numeric part, plus quotation marks
                        propertyValue = propertyValue.substring(0, propertyValue.indexOf("^^"));
                    }
                    //Remove quotation marks
                    propertyValue = propertyValue.replace("\"", "");
                    //Rest should be a number. Try to parse. If it fails, we use default depth, see caught exception
                    depth = Integer.parseInt(propertyValue);
                    if (depth > maxDepth) {
                        //Only allow up to a certain depth
                        depth = maxDepth;
                    }
                } catch (NumberFormatException e) {
                    //Invalid depth provided. For debugging, we are printing this, but otherwise ignoring the parameter, using the default value instead
                    logger.warn("Failed to parse depth header of incoming message to a number.", e);
                }
            }
        }
        //Retrieve object with possibly custom depth
        payload = descriptionProvider.getElement(messageAndPayload.getMessage().getRequestedElement(), depth, outputLanguage);
        try {
            //If this point is reached, the retrieval of the requestedElement was successful (otherwise RejectMessageException is thrown)
            //For REST interface, it is useful to know the class of the requested element
            String typeOfRequestedElement;
            if(messageAndPayload.getMessage().getRequestedElement() != null)
            {
                typeOfRequestedElement = descriptionProvider.getTypeOfRequestedElement(messageAndPayload.getMessage().getRequestedElement());
            }
            else
            {
                //No requested element means the root (self-description) was requested
                typeOfRequestedElement = descriptionProvider.selfDescription.getClass().getSimpleName();
            }


            //Wrap the result in a DescriptionResponse MessageAndPayload
            DescriptionResponseMessage descriptionResponseMessage = new DescriptionResponseMessageBuilder()
                    ._correlationMessage_(messageAndPayload.getMessage().getId())
                    ._issued_(CalendarUtil.now())
                    ._issuerConnector_(descriptionProvider.selfDescription.getId())
                    ._modelVersion_(descriptionProvider.selfDescription.getOutboundModelVersion())
                    ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                    ._senderAgent_(responseSenderAgentUri)
                    .build();

            //Attach a custom property, containing the type of the returned element
            descriptionResponseMessage.setProperty("elementType", typeOfRequestedElement);

            //Attach which serialization was used. This is important for Content Negotiation headers
            descriptionResponseMessage.setProperty("Serialization", outputLanguage.toString());

            //Wrap the result in a DescriptionResult MessageAndPayload
            return new DescriptionResponseMAP(descriptionResponseMessage,
                    payload //Payload is the JSON-LD representation of the requested element
            );
        }
        catch (TokenRetrievalException e) //occurs if we cannot fetch our own security token
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * Determines whether an incoming request can be handled by this class
     * @return true, if the message is of an applicable type (DescriptionRequestMessage only)
     */
    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(DescriptionRequestMessage.class);
    }

}
