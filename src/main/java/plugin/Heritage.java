package plugin;
import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
@AutoService(interpreter.class)
public class Heritage implements interpreter{

    private String className =null;
    private String file =null;
    private Element node=null;

    @Override
    public String getName() {
        return "Heritage";
    }

    @Override
    public boolean checConstruct(Element node) {
        for (Element child:node.getChildren()) {
            if(Objects.equals(child.getName(), "file")){
                file = child.getAttributeValue("path");
            } else if (Objects.equals(child.getName(), "class")) {
                className = child.getAttributeValue("name");
            }
        }
        this.node = node;
        return className != null && file != null;
    }

    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) {

    }

    @Override
    public void construct(Element node, Map<String, String> importer) {
    }
}
