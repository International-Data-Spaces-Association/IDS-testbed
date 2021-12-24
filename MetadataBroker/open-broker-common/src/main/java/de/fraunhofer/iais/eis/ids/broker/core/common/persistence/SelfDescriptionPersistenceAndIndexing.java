package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.broker.core.common.impl.SelfDescriptionPersistenceAdapter;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.index.common.persistence.*;
import de.fraunhofer.iais.eis.ids.index.common.persistence.spi.Indexing;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * This class takes care of persisting and indexing any changes to connectors that are announced to the broker
 */
public class SelfDescriptionPersistenceAndIndexing extends SelfDescriptionPersistenceAdapter {

    private final Logger logger = LoggerFactory.getLogger(SelfDescriptionPersistenceAndIndexing.class);
    private final ConnectorModelCreator connectorModelCreator = new ConnectorModelCreator();

    private final RepositoryFacade repositoryFacade;
    private Indexing<InfrastructureComponent> indexing;

    private static URI componentCatalogUri;

    static Map<URI, URI> replacedIds;

    /**
     * Constructor
     *
     * @param repositoryFacade repository (triple store) to which the modifications should be stored
     */
    public SelfDescriptionPersistenceAndIndexing(RepositoryFacade repositoryFacade, URI componentCatalogUri, Indexing<InfrastructureComponent> indexing) {
        this.repositoryFacade = repositoryFacade;
        this.indexing = indexing;
        SelfDescriptionPersistenceAndIndexing.componentCatalogUri = componentCatalogUri;
        Date date = new Date();
        Timer timer = new Timer();

        //Regularly recreate the index to keep index and triple store in sync
        //The triple store is considered as single source of truth, so the index is dropped and recreated from the triple store
        timer.schedule(new TimerTask() {
            public void run() {
                refreshIndex();
            }
        }, date, 12 * 60 * 60 * 1000); //12*60*60*1000 add 12 hours delay between job executions.

        Serializer.addKnownNamespace("owl", "http://www.w3.org/2002/07/owl#");
    }

    public void setIndexing(Indexing<InfrastructureComponent> indexing)
    {
        this.indexing = indexing;
    }


    /**
     * Setter for the context document URL. Typically extracted from the application.properties
     *
     * @param contextDocumentUrl the context document URL to be used
     */
    public void setContextDocumentUrl(String contextDocumentUrl) {
        connectorModelCreator.setContextFetchStrategy(JsonLdContextFetchStrategy.FROM_URL, contextDocumentUrl);
    }

    /**
     * Function to refresh the index. The index is dropped entirely and recreated from the triple store
     * This keeps the index and triple store in sync, while respecting the triple store as single source of truth
     */
    public void refreshIndex() {
        //Recreate the index to delete everything
        try {
            logger.info("Refreshing indices.");
            indexing.recreateIndex("registrations");

            //If exists, recreate the separate resources index, too
            try {
                indexing.recreateIndex("resources");
            }
            catch (Exception ignored) {}

            List<String> activeGraphs = repositoryFacade.getActiveGraphs();
            if(activeGraphs.isEmpty()) //Nothing to index. Return here to make sure that in case no active graphs exist, inactive ones are also ignored
            {
                return;
            }

            //Iterate over all active graphs, i.e. non-passivated and non-deleted graphs
            for (String graph : activeGraphs) {
                try { //Do a try-catch here, so that one problematic connector does not destroy the entire reindexing process
                    //Add each connector to the index
                    logger.info("Adding connector " + graph + " to index.");
                    indexing.add(repositoryFacade.getConnectorFromTripleStore(new URI(graph)));
                }
                catch (IOException | URISyntaxException | RejectMessageException e) {
                    logger.error("Failed to re-index connector " + graph, e);
                }
            }
        } catch (ConnectException ignored) {
            logger.warn("Could not connect to indexing. Ignoring recreation of index.");
        } //Prevent startup error in case no indexing was started
        catch (IOException e) {
            logger.error("Failed to refresh index: ", e);
        }
    }

    /**
     * Small utility function to replace URIs in a string
     *
     * @param input  String in which URI is to be replaced
     * @param oldURI old URI
     * @param newURI new URI
     * @return updated string
     */
    static private String doReplace(String input, URI oldURI, URI newURI) {
        //Store the original URI, so that we can add an owl:sameAs statement, indicating the original URI
        replacedIds.put(oldURI, newURI);
        //Make sure that we replace only "full URIs" and don't replace the URI if it is only part of a longer URI
        return input.replace("\"" + oldURI + "\"", "\"" + newURI + "\"");
    }

    /**
     * Minimal utility function to turn a connector into a URI matching the REST scheme
     *
     * @param connectorUri Original connector URI
     * @return new connector URI
     */
    static URI rewriteConnectorUri(URI connectorUri) {
        return URI.create(componentCatalogUri.toString() + connectorUri.hashCode());
    }

    /**
     * Main rewrite function, rewriting all URIs contained in a Resource object to match the REST scheme of this broker
     *
     * @param currentString Resource as String (possibly already partly translated)
     * @param resource      Resource as Object
     * @param catalogUri    URI of the catalog containing the Resource
     * @return Resource in String representation with rewritten URIs
     * @throws URISyntaxException, if malformed URIs are encountered
     */
    static String rewriteResource(String currentString, Resource resource, URI catalogUri) throws URISyntaxException {
        //Was the resource rewritten already?
        if(resource.getId().toString().startsWith(componentCatalogUri.toString()))
        {
            return currentString;
        }
        URI resourceUri = new URI(catalogUri + "/" + resource.getId().hashCode());

        //First big block is about contracts attached to a resource
        if (resource.getContractOffer() != null && !resource.getContractOffer().isEmpty()) {
            for (ContractOffer contractOffer : resource.getContractOffer()) {
                //Replace original URI of contract offer with a new one, which is in "our domain"
                //This allows us to provide further details on this object if requested
                URI contractOfferUri = new URI(resourceUri + "/" + contractOffer.getId().hashCode());
                currentString = doReplace(currentString, contractOffer.getId(), contractOfferUri);

                //There can be a number of different Rules: Obligations/Duties, Prohibitions and Permissions
                Map<Rule, URI> allRules = new HashMap<>();
                if (contractOffer.getObligation() != null && !contractOffer.getObligation().isEmpty()) {
                    for (Duty duty : contractOffer.getObligation()) {
                        allRules.put(duty, new URI(contractOfferUri.toString() + "/" + duty.getId().hashCode()));
                    }
                }
                if (contractOffer.getPermission() != null && !contractOffer.getPermission().isEmpty()) {
                    for (Permission permission : contractOffer.getPermission()) {
                        allRules.put(permission, new URI(contractOfferUri.toString() + "/" + permission.getId().hashCode()));
                        if (permission.getPreDuty() != null && !permission.getPreDuty().isEmpty()) {
                            for (Duty duty : permission.getPreDuty()) {
                                allRules.put(duty, new URI(contractOfferUri.toString() + "/" + permission.getId().hashCode() + "/" + duty.getId().hashCode()));
                            }
                        }
                        if (permission.getPostDuty() != null && !permission.getPostDuty().isEmpty()) {
                            for (Duty duty : permission.getPostDuty()) {
                                allRules.put(duty, new URI(contractOfferUri.toString() + "/" + permission.getId().hashCode() + "/" + duty.getId().hashCode()));
                            }
                        }
                    }
                }
                if (contractOffer.getProhibition() != null && !contractOffer.getProhibition().isEmpty()) {
                    for (Prohibition prohibition : contractOffer.getProhibition()) {
                        allRules.put(prohibition, new URI(contractOfferUri.toString() + "/" + prohibition.getId().hashCode()));
                    }
                }
                if (!allRules.isEmpty()) {
                    for (Map.Entry<Rule, URI> ruleEntry : allRules.entrySet()) {
                        currentString = doReplace(currentString, ruleEntry.getKey().getId(), ruleEntry.getValue());
                        if (ruleEntry.getKey().getConstraint() != null && !ruleEntry.getKey().getConstraint().isEmpty()) {
                            for (AbstractConstraint abstractConstraint : ruleEntry.getKey().getConstraint()) {
                                currentString = doReplace(currentString, abstractConstraint.getId(), new URI(ruleEntry.getValue() + "/" + abstractConstraint.getId().hashCode()));
                            }
                        }
                    }
                }

                if (contractOffer.getContractDocument() != null) {
                    currentString = doReplace(currentString, contractOffer.getContractDocument().getId(), new URI(contractOfferUri + "/" + contractOffer.getContractDocument().getId().hashCode()));
                }

            }
        }

        //Contract has been handled. Next, rewrite the URI of the Resource itself
        currentString = doReplace(currentString, resource.getId(), resourceUri);

        //Iterate over endpoints. For each present, replace URI
        if (resource.getResourceEndpoint() != null && !resource.getResourceEndpoint().isEmpty()) {
            for (ConnectorEndpoint connectorEndpoint : resource.getResourceEndpoint()) {
                URI endpointUri = new URI(resourceUri + "/" + connectorEndpoint.getId().hashCode());
                if (connectorEndpoint.getEndpointArtifact() != null) {
                    currentString = doReplace(currentString, connectorEndpoint.getEndpointArtifact().getId(), new URI(endpointUri + "/" + connectorEndpoint.getEndpointArtifact().getId().hashCode()));
                }

                currentString = doReplace(currentString, connectorEndpoint.getId(), endpointUri);
            }

        }

        //Iterate over Representations. If Representation present, adapt string of Representation and, if present, Artifact
        if (resource.getRepresentation() != null) {
            for (Representation representation : resource.getRepresentation()) {
                URI representationURI = new URI(resourceUri + "/" + representation.getId().hashCode());
                currentString = doReplace(currentString, representation.getId(), representationURI);
                if (representation.getInstance() != null) {
                    for (RepresentationInstance artifact : representation.getInstance()) {
                        currentString = doReplace(currentString, artifact.getId(), new URI(representationURI + "/" + artifact.getId().hashCode()));
                    }
                }
            }
        }

        return currentString;
    }

    /**
     * This function replaces URIs of an infrastructure component (component + catalog + resources + representations + artifacts)
     * The new URIs match a scheme suitable for a RESTful API
     *
     * @param infrastructureComponent original InfrastructureComponent
     * @return new InfrastructureComponent with different IDs
     * @throws IOException        if parsing of the updated component fails
     * @throws URISyntaxException if an invalid URI is created during this process
     */
    private InfrastructureComponent replaceIds(InfrastructureComponent infrastructureComponent) throws IOException, URISyntaxException, RejectMessageException {
        //Collect all relevant IDs of IDS items (connector, catalogs, resources, representations, artifacts) replace them later
        //New object is handled, reset the replaced IDs
        replacedIds = new HashMap<>();
        //TODO: Ideally, use relative URIs: "./ + hashCode" instead, but Serializer (Jena) fails on that. We don't really want to store the full URI here, as that makes the broker un-portable
        if (infrastructureComponent.getId() == null) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("Connector did not provide a URI"));
        }

        //Rewrite URI of the connector
        URI infrastructureComponentUri = rewriteConnectorUri(infrastructureComponent.getId());
        String currentString = infrastructureComponent.toRdf();
        currentString = doReplace(currentString, infrastructureComponent.getId(), infrastructureComponentUri);

        //If connector is holding catalogs, rewrite them and their contents
        if (((Connector) infrastructureComponent).getResourceCatalog() != null) {
            for (ResourceCatalog resourceCatalog : ((Connector) infrastructureComponent).getResourceCatalog()) {
                URI catalogUri = new URI(infrastructureComponentUri + "/" + resourceCatalog.getId().hashCode());
                currentString = doReplace(currentString, resourceCatalog.getId(), catalogUri);

                Set<Resource> resourcesToHandle = new HashSet<>();
                if (resourceCatalog.getOfferedResource() != null) {
                    resourcesToHandle.addAll(resourceCatalog.getOfferedResource());
                }
                if (resourceCatalog.getRequestedResource() != null) {
                    resourcesToHandle.addAll(resourceCatalog.getRequestedResource());
                }
                for (Resource currentResource : resourcesToHandle) {
                    currentString = rewriteResource(currentString, currentResource, catalogUri);
                }
            }
        }
        //Now that we replaced all the IDs, add owl:sameAs statements and then parse
        return new Serializer().deserialize(addSameAsStatements(currentString), InfrastructureComponent.class);
    }

    /**
     * This internal function adds the replaced URIs as owl:sameAs statements to preserve the original URIs
     *
     * @param jsonLd RDF string after replacements
     * @return Apache Jena Model with additional sameAs statements
     */
    static Model addSameAsStatements(String jsonLd) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new ByteArrayInputStream(jsonLd.getBytes(StandardCharsets.UTF_8)), RDFLanguages.JSONLD);
        for (Map.Entry<URI, URI> entry : replacedIds.entrySet()) {
            model.add(ResourceFactory.createStatement( //Add a new triple to the model
                    ResourceFactory.createResource(entry.getValue().toString()), //Subject: The new URI
                    ResourceFactory.createProperty("http://www.w3.org/2002/07/owl#sameAs"), //Predicate: owl:sameAs
                    ResourceFactory.createResource(entry.getKey().toString()))); //Object: The original URI
        }
        return model;
    }


    /**
     * Function to persist and index modifications to an existing connector
     *
     * @param infrastructureComponent The updated connector which was announced to the broker
     * @throws IOException            thrown, if the connection to the repository could not be established
     * @throws RejectMessageException thrown, if the update is not permitted, e.g. because the connector was previously deleted, or if an internal error occurs
     */
    @Override
    public URI updated(InfrastructureComponent infrastructureComponent) throws IOException, RejectMessageException {
        URI connectorUri = rewriteConnectorUri(infrastructureComponent.getId());
        boolean wasActive = repositoryFacade.graphIsActive(connectorUri.toString());
        boolean existed = repositoryFacade.graphExists(connectorUri.toString());

        //Replace URIs in this infrastructureComponent with URIs matching our scheme. This is required for a RESTful API
        //TODO: Do the same for resources (or at ParIS, for participants)
        try {
            infrastructureComponent = replaceIds(infrastructureComponent);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        if (!existed) {
            logger.info("New connector registered: " + infrastructureComponent.getId().toString());
            addToTriplestore(infrastructureComponent.toRdf());
        } else {
            logger.info("Updating a connector which is already known to the broker: " + infrastructureComponent.getId().toString());
            updateTriplestore(infrastructureComponent.toRdf());
        }
        //We need to reflect the changes in the index.
        //If the connector was passive before, the document was deleted from the index, so we need to recreate it
        if (wasActive) { //Connector exists in index - update it
            try {
                indexing.update(infrastructureComponent);
            } catch (Exception e) {
                if (e.getMessage().contains("document_missing_exception")) {
                    indexing.add(infrastructureComponent);
                } else {
                    logger.error("ElasticsearchStatusException caught with message " + e.getMessage());
                    throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
                }
            }
        } else { //Connector does not exist in index - create it
            indexing.add(infrastructureComponent);
        }
        //return the (rewritten) URI of the infrastructure component
        return infrastructureComponent.getId();
    }

    /**
     * Internal function which should only be called from the available function. It applies the changes to the triple store
     *
     * @param selfDescriptionJsonLD String representation of the connector to be added to triple store
     * @throws IOException            thrown, if the changes could not be applied to the triple store
     * @throws RejectMessageException thrown, if the changes are illegal, or if an internal error has occurred
     */
    private void addToTriplestore(String selfDescriptionJsonLD) throws IOException, RejectMessageException {
        ConnectorModelCreator.InnerModel result = connectorModelCreator.toModel(selfDescriptionJsonLD);
        repositoryFacade.addStatements(result.getModel(), result.getNamedGraph().toString());
    }

    /**
     * Internal function which should only be called from the updated function. It applies the changes to the triple store
     *
     * @param selfDescriptionJsonLD String representation of the connector which needs to be updated
     * @throws IOException thrown, if the changes could not be applied to the triple store
     */
    private void updateTriplestore(String selfDescriptionJsonLD) throws IOException, RejectMessageException {
        ConnectorModelCreator.InnerModel result = connectorModelCreator.toModel(selfDescriptionJsonLD);
        repositoryFacade.replaceStatements(result.getModel(), result.getNamedGraph().toString());
    }

    /**
     * Function to mark a given Connector as deleted/passivated in the triple store and delete the Connector from the index
     *
     * @param issuerConnector A URI reference to the connector which is now inactive
     * @throws IOException            if the connection to the triple store could not be established
     * @throws RejectMessageException if the operation is not permitted, e.g. because one is trying to passivate a Connector which was previously deleted or due to an internal error
     */
    @Override
    public void unavailable(URI issuerConnector) throws IOException, RejectMessageException {
        //Turn graph into a passive one
        URI rewrittenConnectorUri = rewriteConnectorUri(issuerConnector);
        if (repositoryFacade.graphIsActive(rewrittenConnectorUri.toString())) {
            repositoryFacade.changePassivationOfGraph(rewrittenConnectorUri.toString(), false);
        } else {
            throw new RejectMessageException(RejectionReason.NOT_FOUND, new NullPointerException("The connector you are trying to remove was not found"));
        }

        //Remove the passivated graph from indexing. Upon re-activating, this will be undone
        indexing.delete(rewrittenConnectorUri);
    }


    /**
     * Utility function to evaluate a given query (in a re-formulated way, respecting passivation and hiding underlying structure of named graphs)
     *
     * @param queryString Query to be evaluated
     * @return Query result in String format
     * @throws RejectMessageException, if the query is illegal or if the index is empty
     */
    @Override
    public String getResults(String queryString) throws RejectMessageException {
        return new GenericQueryEvaluator(repositoryFacade).getResults(queryString);
    }
}
