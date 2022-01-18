package de.fraunhofer.iais.eis.ids.index.common.persistence;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class which provides some utility for handling SPARQL construct query results, such as parsing the result to a Connector, Participant or Catalog
 */
public class ConstructQueryResultHandler {
    public static String contextDocumentUrl;
    public static String catalogUri;
    private static final Logger logger = LoggerFactory.getLogger(ConstructQueryResultHandler.class);

    /**
     * Takes the result of a Construct Query targeted to retrieve triples about a participant
     * @param result The collection of triples representing the participant
     * @return The participant as a Java object
     * @throws RejectMessageException if the passed result is empty or the serializer encounters an exception
     */
    public static Participant GraphQueryResultToParticipant(Model result) throws RejectMessageException {

        Serializer s = new Serializer();

        try {
            return s.deserialize(graphToString(result), Participant.class);
        }
        catch (IOException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }

    }

    /**
     * Takes the result of a Construct Query targeted to retrieve triples about a connector
     * @param result The collection of triples representing the connector
     * @return The connector as a Java object
     * @throws RejectMessageException if the passed result is empty or the serializer encounters an exception
     */
    public static Connector GraphQueryResultToConnector(Model result) throws RejectMessageException {
        Serializer s = new Serializer();

        try {
            return s.deserialize(graphToString(result), Connector.class);
        }
        catch (IOException e)
        {
            logger.error("Parsing this connector caused an IOException: " + graphToString(result));
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    /**
     * Utility function for turning an Apache Jena Model into a JSON-LD String
     * @param model Input model
     * @return Model as JSON-LD String
     */
    public static String graphToString(Model model)
    {
        RDFWriter writer = RDFWriter.create().format(RDFFormat.JSONLD).source(model).build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.output(os);
        return os.toString();
    }

    public static String graphToString(Model model, Lang lang)
    {
        RDFWriter writer;
        if(lang.equals(Lang.TTL) || lang.equals(Lang.TURTLE))
        {
            writer = RDFWriter.create().format(RDFFormat.TTL).source(model).build();
        }
        else if(lang.equals(Lang.JSONLD))
        {
            writer = RDFWriter.create().format(RDFFormat.JSONLD_COMPACT_PRETTY).source(model).build();
        }
        else if(lang.equals(Lang.N3)|| lang.equals(Lang.NTRIPLES))
        {
            writer = RDFWriter.create().format(RDFFormat.NT).source(model).build();
        }
        else if(lang.equals(Lang.RDFXML))
        {
            writer = RDFWriter.create().format(RDFFormat.RDFXML).source(model).build();
        }
        else
        {
            writer = RDFWriter.create().format(RDFFormat.JSONLD_COMPACT_PRETTY).source(model).build();
            logger.warn("Unsupported serialization requested: " + lang.toString() + " - defaulting to JSON-LD.");
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.output(os);
        return os.toString();
    }

}
