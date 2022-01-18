package de.fraunhofer.iais.eis.ids.broker.core.common.impl;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.InfrastructureComponentStatusHandler;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.QueryResultsProvider;

import java.io.IOException;
import java.net.URI;

/**
 * Abstract class which provides the required functions for building a persistence adapter for IDS Infrastructure Components (typically Connectors)
 */
public abstract class SelfDescriptionPersistenceAdapter implements InfrastructureComponentStatusHandler, QueryResultsProvider {

    //Removed in IM 4.0.0
    //@Override
    //public abstract void available(InfrastructureComponent infrastructureComponent) throws IOException, RejectMessageException;

    /**
     * This function stores a new infrastructure component OR updates an existing infrastructure component in the triple store and updates the index correspondingly
     * It should be called when a ConnectorUpdateMessage was received
     * @param infrastructureComponent The new or updated connector which was announced to the broker
     * @return The new URI of the connector, which may have been rewritten by the broker
     * @throws IOException may be thrown if the connection to the triple store or index fails
     * @throws RejectMessageException may be thrown if, for example, the connector doesn't exist yet or some internal error occurs
     */
    @Override
    public abstract URI updated(InfrastructureComponent infrastructureComponent) throws IOException, RejectMessageException;

    /**
     * This function removes an existing infrastructure component from the triple store and updates the index correspondingly
     * Note that deletion should not be physical, but rather mark the infrastructure component as deleted and treat any queries on the triple store as if this connector didn't exist anymore
     * It should be called when a ConnectorUnavailableMessage was received
     * @param issuerConnector A URI reference to the connector which is now unavailable
     * @throws IOException may be thrown if the connection to the triple store or index fails
     * @throws RejectMessageException may be thrown if, for example, the connector doesn't exist yet or some internal error occurs
     */
    @Override
    public abstract void unavailable(URI issuerConnector) throws IOException, RejectMessageException;

    /**
     * This function provides the possibility to fire a general query at the triple store persisting the infrastructure components
     * @param query Query to be evaluated
     * @return Query result as String
     * @throws RejectMessageException, if the query could not be evaluated, e.g. because the query is illegal (such as accessing a deleted connector)
     */
    @Override
    public abstract String getResults(String query) throws RejectMessageException;

}
