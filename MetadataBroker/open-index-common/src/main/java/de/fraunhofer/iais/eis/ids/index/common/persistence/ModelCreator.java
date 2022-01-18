package de.fraunhofer.iais.eis.ids.index.common.persistence;

import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Optional;

/**
 * Class to create a model for Connectors, Resources or Participants. This class is overridden from within the broker and ParIS, respectively.
 * Used within the *PersistenceAndIndexing, e.g. ResourcePersistenceAndIndexing, to turn an object into a model (list of statements) which can be passed to the the triple store
 */
public abstract class ModelCreator {
    final private Logger logger = LoggerFactory.getLogger(ModelCreator.class);
    private JsonLdContextFetchStrategy contextFetchStrategy = JsonLdContextFetchStrategy.FROM_CLASSPATH;
    private String contextDocumentUrl;
    private String contextDocument = "";

    /**
     * The model of the actual Connector, Resource or Participant
     */
    public static class InnerModel {
        private final Model model;
        private final Resource namedGraph;

        /**
         * Constructor
         * @param model The RDF4J Model
         * @param namedGraph The named graph in which the model is or will be stored
         */
        InnerModel(Model model, Resource namedGraph) {
            this.model = model;
            this.namedGraph = namedGraph;
        }

        /**
         * Accessor for the model
         * @return an RDF4J Model
         */
        public Model getModel() {
            return model;
        }

        /**
         * Accessor for the named graph
         * @return the named graph in which the model is or will be stored
         */
        public Resource getNamedGraph() {
            return namedGraph;
        }
    }

    /**
     * Function to change the context fetch strategy, e.g. to "Download context" or "Search for context locally"
     * @param contextFetchStrategy the new context fetch strategy
     * @param contextDocumentUrl the URL of the context
     */
    public void setContextFetchStrategy(JsonLdContextFetchStrategy contextFetchStrategy, String contextDocumentUrl) {
        this.contextFetchStrategy = contextFetchStrategy;
        this.contextDocumentUrl = contextDocumentUrl;
    }

    /**
     * Function to retrieve the context document from the local classpath
     * @return context document as String
     */
    private String getContextDocFromClasspath() {
        try {
            //TODO: .jsonld instead of .json?
            return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("context.json"), Charset.defaultCharset());
        }
        catch (IOException e) {
            logger.warn("Unable to load JSON-LD context document from classpath.", e);
        }
        return "";
    }

    /**
     * Function to retrieve context document from the web
     * @param contextDocumentUrl the URL from which the context document can be retrieved
     * @return context document as String
     */
    private String getContextDocFromUrl(String contextDocumentUrl) {
        try {
            return IOUtils.toString(new InputStreamReader(new URL(contextDocumentUrl).openStream()));
        }
        catch (IOException e) {
            logger.warn("Unable to fetch JSON-LD context document from '" + contextDocumentUrl + "'", e);
        }
        return "";
    }

    /**
     * Function to retrieve an InnerModel (e.g. a model of a Connector, Resource or Participant) of some string representation
     * @param selfDescription A Connector, Resource or Participant as String
     * @return An InnerModel, containing an RDF4J Model which can be used for extracting RDF statements
     * @throws IOException if the selfDescription cannot be parsed
     */
    public InnerModel toModel(String selfDescription) throws IOException, RejectMessageException {
        Model model = parse(selfDescription);
        Optional<Resource> namedGraph = determineNamedGraph(model);

        return new InnerModel(model, namedGraph.orElse(null));
    }

    /**
     * Function to parse a String representation in some RDF Format into an Apache Jena Model
     * @param selfDescriptionJsonLd String representation of the object to be parsed (Connector, Resource or Participant)
     * @return a Model representation of selfDescriptionJsonLd
     * @throws IOException if the self description cannot be processed (streamed)
     */
    Model parse(String selfDescriptionJsonLd)
            throws IOException
    {
        InputStream in = IOUtils.toInputStream(selfDescriptionJsonLd, "UTF-8");
        return ModelFactory.createDefaultModel().read(in, null, RDFLanguages.strLangJSONLD);
    }

    /**
     * Function to retrieve the JSON-LD context, either from the web or from the classpath, depending on options
     * @param options Options determining where the context should be fetched from
     */
    private void fetchJsonLdContext(JsonLdOptions options) throws JsonLdError {
        if (contextDocument.isEmpty()) {
            switch (contextFetchStrategy) {
                case FROM_URL:
                    contextDocument = getContextDocFromUrl(contextDocumentUrl);
                    break;

                default:
                case FROM_CLASSPATH:
                    contextDocument = getContextDocFromClasspath();
            }
        }

        DocumentLoader dl = new DocumentLoader();
        String CONTEXT_URL = "https://w3id.org/idsa/contexts/context.jsonld";
        dl.addInjectedDoc(CONTEXT_URL,  contextDocument);
        options.setDocumentLoader(dl);
    }

    /**
     * Determines the URI of the named graph of the model. In case of a Participant or Connector, it is their ID. In case of a Resource, it is the ID of the containing connector
     * @param model Model for which the named graph should be determined
     * @return the named graph in form of an RDF4J Resource
     */
    public Optional<Resource> determineNamedGraph(Model model) throws RejectMessageException {
        Iterator<Statement> iterator = model.listStatements();
        Optional<Resource> subject = Optional.empty();
        while(iterator.hasNext())
        {
            Statement st = iterator.next();
            if(subjectIsInstanceInnerModel(st))
            {
                subject = Optional.of(st.getSubject());
                break;
            }
        }
        return subject;
    }

    /**
     * Function which determines whether a given Statement is an object of the inner model, e.g. if a Statement defines a Connector, a Resource or a Participant
     * @param statement The statement to be tested
     * @return true, if it is an instance of the inner model, otherwise false
     */
    public abstract boolean subjectIsInstanceInnerModel(Statement statement);
}