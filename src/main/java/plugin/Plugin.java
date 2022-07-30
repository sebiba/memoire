package plugin;

import Interfaces.Interpreter;
import com.google.auto.service.AutoService;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Element;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


@AutoService(Interpreter.class)
public class Plugin implements Interpreter {
    private String remote =null;

    @Override
    public String getName() {return "Plugin";}
    @Override
    public boolean checConstruct(Element node) {
        for (Element child:node.getChildren()) {
            if(Objects.equals(child.getName(), "file")){
                return child.getAttributeValue("path").isEmpty();
            }
        }
        return false;
    }
    @Override
    public void construct(Element node, Map<String, String> importer) {
        this.remote = node.getAttributeValue("url");
        if(Objects.equals(this.remote, "") || this.remote == null){
            if(new Importer().isAnUrl(importer.values().toArray()[0].toString())){
                List<String> path = List.of(new File(String.valueOf(importer.values().toArray()[0])).getParent()
                    .split(Pattern.quote(System.getProperty("file.separator"))));
                this.remote = String.join("/",path.subList(0, path.size()-1))+"/tree/"+node.getAttributeValue("name");
            }else{
                this.remote = node.getAttributeValue("name");
            }
        }
        for (Element child : node.getChildren()) {
            if (Objects.equals(child.getName(), "file")) {
                this.checImport(new File(importer.keySet().toArray()[0].toString()).getParent(),
                                importer,
                                child.getAttributeValue("path"));
            }
        }
    }

    @Override
    public void setConfigFile(Element node) {

    }

    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) {
        List<String> path = List.of(new File(String.valueOf(importer.values().toArray()[0])).getParent()
            .split(Pattern.quote(System.getProperty("file.separator"))));
        String director = String.join("\\",path.subList(0, path.size()-1));
        if(!JavaFileManager.getInstance().isFileInProjectDirectory(file)) {
            if(!this.remote.startsWith("\\") && !file.endsWith("\\")){
                file = "\\".concat(file);
            }
            //TODO: change as in Aspect.java
            JavaFileManager.getInstance().copyFileFrom(director + "\\" + this.remote + file, localDirect + file);
        }
    }
}
