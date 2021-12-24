package de.fraunhofer.iais.eis.ids.index.common.persistence;

/**
 * This enum describes the possible strategies with which a context can be fetched
 * Options are to get it locally (FROM_CLASSPATH) or to fetch it online (FROM_URL)
 */
public enum JsonLdContextFetchStrategy {

    FROM_CLASSPATH, FROM_URL

}
