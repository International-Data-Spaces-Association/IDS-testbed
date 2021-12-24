package de.fraunhofer.iais.eis.ids.index.common.persistence;


import java.net.URI;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.index.common.persistence.spi.Indexing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy indexing that can accept any kind of class from the IDS information model and ignores the indexing
 */
public class NullIndexing<T> implements Indexing<T> {

    final private Logger logger = LoggerFactory.getLogger(NullIndexing.class);

    @Override
    public void add(T selfDescription) {
        logger.info("Add to index IGNORED.");
    }

    @Override
    public void update(T selfDescription) {
        logger.info("Index update IGNORED.");
    }

    @Override
    public void updateResource( Connector reducedConnector, Resource resource )
           {
               logger.info("Resource update from index IGNORED.");
           }

    @Override
    public void delete(URI issuerConnector) {
        logger.info("Delete from index IGNORED.");
    }


    @Override
    public void recreateIndex(String indexName) {
        logger.info("Clear index IGNORED.");
    }

}
