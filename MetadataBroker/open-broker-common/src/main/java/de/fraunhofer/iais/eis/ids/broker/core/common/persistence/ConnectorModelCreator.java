package de.fraunhofer.iais.eis.ids.broker.core.common.persistence;


import de.fraunhofer.iais.eis.ids.index.common.persistence.INFOMODEL;
import de.fraunhofer.iais.eis.ids.index.common.persistence.ModelCreator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

/**
 * This class is an extension to the ModelCreator class, providing functionality to determine whether an RDF triple defines a Connector
 */
public class ConnectorModelCreator extends ModelCreator {

    /**
     * This function determines whether a statement defines a Connector
     * @param statement The statement to be tested
     * @return true, if it defines a connector, otherwise false
     */
    @Override
    public boolean subjectIsInstanceInnerModel(Statement statement) {
        return statement.getPredicate().equals(RDF.type) &&
                (statement.getObject().equals(INFOMODEL.BASE_CONNECTOR) ||
                        statement.getObject().equals(INFOMODEL.TRUSTED_CONNECTOR));
    }

}
