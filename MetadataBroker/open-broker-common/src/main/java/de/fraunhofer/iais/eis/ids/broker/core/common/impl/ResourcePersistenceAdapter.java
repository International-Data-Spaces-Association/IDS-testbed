package de.fraunhofer.iais.eis.ids.broker.core.common.impl;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.broker.core.common.persistence.ResourceStatusHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.QueryResultsProvider;

import java.io.IOException;
import java.net.URI;

/**
 * Abstract class which provides the required functions for building a persistence adapter for IDS Resources
 */
public abstract class ResourcePersistenceAdapter implements ResourceStatusHandler, QueryResultsProvider {

    /**
     * This function updates an existing resource or inserts a new resource into the triple store and updates the index correspondingly
     * It should be called when a ResourceUpdateMessage was received
     * @param resource The new / updated resource which was announced to the broker
     * @param connectorUri The connector which is offering the resource
     * @return The new URI of the resource, which may have been rewritten by the broker
     * @throws IOException may be thrown if the connection to the triple store or index fails
     * @throws RejectMessageException may be thrown if, for example, the resource doesn't exist yet or some internal error occurs
     */
    @Override
    public abstract URI updated(Resource resource, URI connectorUri) throws IOException, RejectMessageException;

    /**
     * This function removes an existing resource from the triple store and updates the index correspondingly
     * It should be called when a ResourceUnavailableMessage was received
     * @param resourceUri A URI reference to the resource which is now unavailable
     * @param connectorUri The connector which used to offer the resource
     * @throws IOException may be thrown if the connection to the triple store or index fails
     * @throws RejectMessageException may be thrown if, for example, the resource doesn't exist yet or some internal error occurs
     */
    @Override
    public abstract void unavailable(URI resourceUri, URI connectorUri) throws IOException, RejectMessageException;

    /**
     * This function provides the possibility to fire a general query at the triple store persisting the resources
     * @param query Query to be evaluated
     * @return Query result as String
     */
    @Override
    public abstract String getResults(String query) throws RejectMessageException;

}
