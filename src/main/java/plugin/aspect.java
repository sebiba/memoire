package plugin;

import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@AutoService(interpreter.class)
public class aspect implements interpreter{
    private String className =null;
    private String file =null;
    private Element node=null;
    private Map<String, String> importer = new HashMap<>();
    @Override
    public String getName() {
        return "SpringAspect";
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
    public void checImport(Element node, Map<String, String> importerParam) {
        for (Attribute attr:node.getAttributes()) {
            if(!attr.getValue().equals("")){
                this.importer.put(attr.getName(), attr.getValue());
            }
        }
        if (!importerParam.isEmpty()){
            for (Map.Entry<String, String> attr:importerParam.entrySet()) {
                this.importer.putIfAbsent(attr.getKey(), attr.getValue());
            }
        }
    }
    @Override
    public void getChildren() {
    }
    @Override
    public void getAttributs() {

    }
    @Override
    public void prettyPrint() {
        System.out.println("$$$$$$$$$$$$$$$$$Aspect$$$$$$$$$$$$$$$$");
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
        if(lib.JavaFileManager.getInstance().isFileInProjectDirectory(file)){
            System.out.println("present");
        }else{
            new importer(node).loadFile(this.importer.get("uri"), this.file);
            System.out.println("absent");
        }
    }
}
