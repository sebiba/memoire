package plugin;
import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
@AutoService(interpreter.class)
public class heritage implements interpreter{

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
    public void checImport(Element node, Map<String, String> importer) {

    }
    @Override
    public void getChildren() {

    }

    @Override
    public void getAttributs() {

    }

    @Override
    public void prettyPrint() {
        System.out.println("$$$$$$$$$$$$$$$$$Héritage$$$$$$$$$$$$$$$$");
        Map<String, String> map = new HashMap<String, String>();
        for (Element child:this.node.getChildren()) {
            map.put(child.getName(), String.valueOf(child.getAttributes()));
        }
        for (int i = 0; i < map.size(); i++) {
            System.out.print(map.keySet().toArray()[i]+"\t");
            System.out.print(map.values().toArray()[i]+"\n");
        }
        System.out.println(map.toString());
    }

    @Override
    public void insert() {

    }
}
