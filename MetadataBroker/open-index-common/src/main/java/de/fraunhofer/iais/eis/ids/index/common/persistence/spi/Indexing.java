package de.fraunhofer.iais.eis.ids.index.common.persistence.spi;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;

/**
 * Interface for providing indexing functionality for infrastructure components and participants
 * Note that Resources are handled by the ResourceStatusHandler instead, as it is broker specific and not common with the ParIS
 */
public interface Indexing<T> {

    /**
     * Function for adding an infrastructure component, such as a connector, to the index
     * @param object The object to be indexed
     * @throws IOException may be thrown on error
     */
    void add(T object) throws IOException;

    /**
     * Function for updating an already indexed infrastructure component
     * @param object The object in its current form for updating the index
     * @throws IOException may be thrown if the infrastructure component could not be updated, e.g. because it was not found
     */
    void update(T object) throws IOException;

    /**
     * Function for updating an resource in the index
     * @param reducedConnector Connector with resources as list of URIs
     * @param resource Resource to be indexed
     * @throws IOException may be thrown if the infrastructure component could not be updated, e.g. because it was not found
     */
    void updateResource( Connector reducedConnector, Resource resource ) throws IOException;

    /**
     * Function for removing an indexed infrastructure component OR participant from the index
     * @param objectId A reference to the object to be removed
     * @throws IOException if the infrastructure component could not be deleted, e.g. because it was not found
     */
    void delete(URI objectId) throws IOException;

    /**
     * Function for recreating the entire index from the current state of the repository (triple store). This helps keeping database and index in sync
     * @param indexName name of the index to be recreated
     * @throws IOException may be thrown if an exception occurs during the dropping or recreation of the index
     */
    void recreateIndex(String indexName) throws IOException;

}
