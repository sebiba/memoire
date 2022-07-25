import Interfaces.interpreter;
import exceptions.RequirementException;
import exceptions.StructureNotSupportedException;
import lib.Importer;
import lib.JavaFileManager;
import lib.xmlParser;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.util.*;

public class main_engine {
    public static void main(String[] args) {
        Document document = JavaFileManager.getInstance().getXmlFile("src/main/resources/config.xml");
        assert document != null;
        Element racine = document.getRootElement();
        switch (racine.getName()){
            case "Configuration":
                try {
                    buildConfig(racine);
                }catch (RequirementException e){
                    e.printStackTrace();
                }
                break;
            case "FeatureModel":
                checFeatureModel(racine);
                break;
        }
        //JavaFileManager.getInstance().deleteFile("temp");
    }

    /**
     * build de desired configuration corresponding to the xml file
     * @param racine root xml element from the xml file
     */
    public static void buildConfig(Element racine) throws RequirementException {
        Map<String, interpreter> plugins = loadPlugins();
        Importer importer = new Importer(racine);
        List<String> gitBranches = new ArrayList<>();
        if(importer.isSourceGitRepo()){
            try {
                String url = importer.getRemoteImport().substring(0,importer.getRemoteImport().lastIndexOf("/"));
                gitBranches = JavaFileManager.getInstance().getBranchesFromGitRepo(url);
            } catch (GitAPIException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(!importer.checSelection(racine)){
            throw new RequirementException("Erreur lors de la verification des requirements");
        }
        for (Element node: xmlParser.getInstance().getChildOf(racine)) {
            //load import attr in map to pass to each variant
            if(!node.getName().equals("import")){
                //apply every plugin to corresponding variant
                try {
                    //config file
                    if(node.getChildren().size()<1){
                        if(importer.isSourceGitRepo()){
                            if(!gitBranches.contains(node.getAttributeValue("name")) && !gitBranches.isEmpty()){
                                throw new StructureNotSupportedException("The branches '"+node.getAttributeValue("name")+"' has not been found on github");
                            }
                        }
                        plugins.get(node.getName()).construct(importer.getFeatureModelFor(node.getAttribute("name").getValue()),
                                    importer.getImport());

                    }
                    //featureModel file
                    else{
                        plugins.get(node.getName()).checConstruct(node);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //TODO: make preprocessor juste befor compile
        System.out.println("configuration assemblée");
        //compileManager.getInstance().maven_powerShell("temp", "compile");
        System.out.println("compilation effectuée");
        //compileManager.getInstance().maven_powerShell("temp", "package -DskipTests");
        System.out.println("packagation effectuée");
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
