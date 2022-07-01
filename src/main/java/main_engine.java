import Interfaces.interpreter;
import lib.xmlParser;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import plugin.importer;

import java.io.File;
import java.util.*;

public class main_engine {
    public static void main(String[] args) {
        SAXBuilder sxb = new SAXBuilder();
        Document document = null;
        try {
            document = sxb.build(new File("src/main/resources/config.xml"));
        }
        catch(Exception ignored){}
        assert document != null;
        Element racine = document.getRootElement();
        switch (racine.getName()){
            case "Configuration":
                buildConfig(racine);
                break;
            case "FeatureModel":
                checFeatureModel(racine);
                break;
        }
    }

    /**
     * build de desired configuration corresponding to the xml file
     * @param racine root xml element from the xml file
     */
    public static void buildConfig(Element racine){
        Map<String, interpreter> plugins = loadPlugins();
        Map<String, String> importer = new HashMap<>();
        for (Element node: xmlParser.getInstance().getChildOf(racine)) {
            //load import attr in map to pass to each variant
            if(node.getName().equals("import")){
                List<Attribute> attrList = node.getAttributes();
                for (Attribute attr: attrList) {
                    if(!attr.getValue().equals("")){
                        importer.putIfAbsent(attr.getName(), attr.getValue());
                        new importer(node).load();
                    }
                }
            }
            //apply every plugin to corresponding variant
            try {
                plugins.get(node.getName()).checConstruct(node);
                plugins.get(node.getName()).checImport(node, importer);
                plugins.get(node.getName()).prettyPrint();
                plugins.get(node.getName()).insert();
            }catch (Exception ignored){}
        }
        //compileManager.getInstance().maven_exec("C:\\unamur\\Master\\memoire\\engine2\\temp", "compile");
    }
    public static void checFeatureModel(Element racine){

    }

    /**
     * Load plugin available for prossessing
     * @return Map<String, interpreter> plugin's name and the plugin
     */
    static public Map<String, interpreter> loadPlugins(){
        ServiceLoader<interpreter> serviceLoader = ServiceLoader.load(interpreter.class);
        Map<String, interpreter> services = new HashMap<>();
        for (interpreter service : serviceLoader) {
            services.put(service.getName(), service);
        }
        return services;
    }
}
