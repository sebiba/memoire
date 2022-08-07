package interfaces;

import org.jdom2.Element;

import java.io.IOException;
import java.util.Map;

public interface Interpreter {
    /**
     * get the name of the plugin to identify
     * @return String the name of the plugin
     */
    String getName();

    /**
     * check is the structure of the featureModel is correct
     *
     * @return true if the featureModel's structure is correct false otherwise
     */
    String getxsdDeclaration() throws IOException;

    /**
     * import variant specific file
     * @param localDirect String local folder where to save the file
     * @param importer Map<String, String> path to local and remote featureModel
     * @param file String file to import
     * @throws IOException error while accessing the file
     */
    void checImport(String localDirect, Map<String, String> importer, String file) throws IOException;

    /**
     * apply all the changes needed to link the variant to the rest of the program
     * @param featureModel Element Xml featureModel
     * @param importer Map<String, String> path to local and remote featureModel
     */
    void construct(Element featureModel, Map<String, String> importer);

    /**
     * get access to configuration file to pass variables for exemple
     * @param Configuration Element xml Configuration
     */
    void setConfigFile(Element Configuration);
}
