package plugin;

import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import lib.JavaFileManager;
import org.jdom2.Element;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


@AutoService(interpreter.class)
public class Plugin implements interpreter{
    private String remote =null;
    private String file =null;
    private Map<String, String> importer = new HashMap<>();
    private Element node;

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
        if(this.remote.isEmpty())
            this.remote = node.getAttributeValue("name");
        for (Element child : node.getChildren()) {
            if (Objects.equals(child.getName(), "file")) {
                this.checImport(new File(importer.keySet().toArray()[0].toString()).getParent(),
                                importer,
                                child.getAttributeValue("path"));
            }
        }
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
            JavaFileManager.getInstance().copyFileFrom(director + "\\" + this.remote + file, localDirect + file);
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
        System.out.println("$$$$$$$$$$$$$$$$$Plugin$$$$$$$$$$$$$$$$");
    }

    @Override
    public void insert() {
        if(JavaFileManager.getInstance().isFileInProjectDirectory(file)){
            //boolean test = lib.JavaFileManager.getInstance().deleteFile(file);
            System.out.println("present");
        }else{
            //new Importer(node).loadFile(this.importer.get("uri"), this.file);
            System.out.println("absent");
        }
    }
}
