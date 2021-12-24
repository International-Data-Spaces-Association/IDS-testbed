package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;

import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

import java.io.IOException;
import java.net.URI;

/**
 * Interface which describes the functionality required to provide an indexing service for resources
 */
public interface ResourceStatusHandler {

        /**
         * Function to remove a given Resource from the indexing and the triple store
         * @param resourceUri A URI reference to the resource which is now unavailable
         * @param connectorUri The connector which used to offer the resource
         * @throws IOException may be thrown, if the connection to the triple store could not be established
         * @throws RejectMessageException may be thrown, if the operation is not permitted, e.g. because one is trying to delete the resource from another connector, the resource is not known or due to an internal error
         */
        void unavailable(URI resourceUri, URI connectorUri) throws IOException, RejectMessageException;

        /**
         * Function to persist and index a new resource or modifications made to an existing resource
         * @param resource The new / updated resource which was announced to the broker
         * @param connectorUri The connector which is offering the resource
         * @return The URI of the resource, which was possibly modified by the broker
         * @throws IOException may be thrown, if the connection to the repository could not be established
         * @throws RejectMessageException may be thrown, if the update is not permitted, e.g. because the resource of an inactive connector is modified, or if an internal error occurs
         */
        public URI updated(Resource resource, URI connectorUri) throws IOException, RejectMessageException;

        /**
         * Function to check whether a resource with a given URI exists
         * @param resourceUri The resource URI to be checked for existence
         * @return true, if a resource with this URI exists
         * @throws RejectMessageException if the information cannot be retrieved
         */
        boolean resourceExists(URI resourceUri) throws RejectMessageException;
}
