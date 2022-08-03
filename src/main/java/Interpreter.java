import org.jdom2.Element;

import java.io.IOException;
import java.util.Map;

public interface Interpreter {
    String getName();
    boolean checConstruct(Element node);

    void checImport(String localDirect, Map<String, String> importer, String file) throws IOException;

    void construct(Element featureModel, Map<String, String> importer);

    void setConfigFile(Element Configuration);
}
