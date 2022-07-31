import Interfaces.Interpreter;
import exceptions.RequirementException;
import exceptions.StructureNotSupportedException;
import lib.Importer;
import lib.JavaFileManager;
import lib.compileManager;
import lib.xmlParser;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.util.*;

public class main_engine {
    public static void main(String[] args) {
        if(args.length == 0){
            showHelp();
            return;
        }
        Document document = JavaFileManager.getInstance().getXmlFile(args[0]);
        assert document != null;
        Element racine = document.getRootElement();
        switch (racine.getName()){
            case "Configuration":
                try {
                    buildConfig(racine, args.length > 1 && Boolean.parseBoolean(args[1]));
                }catch (RequirementException e){
                    e.printStackTrace();
                }
                break;
            case "FeatureModel":
                checFeatureModel(racine);
                break;
        }
    }

    /**
     * show message if no parameters founded
     */
    private static void showHelp() {
        System.out.println("Des paramètres sont manquant...");
        System.out.println("paramètres 1: chemin vers le fichier de configuration");
        System.out.println("paramètres 2: Optionnel boolean true ou false pour ne pas executer de tests lors de la génération du fichier jar.");
    }

    /**
     * build de desired configuration corresponding to the xml file
     * @param racine root xml element from the xml file
     */
    public static void buildConfig(Element racine, Boolean withoutTest) throws RequirementException {
        Map<String, Interpreter> plugins = loadPlugins();
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
                    if(importer.isSourceGitRepo()){
                        if(!gitBranches.contains(node.getAttributeValue("name")) && !gitBranches.isEmpty()){
                            throw new StructureNotSupportedException("The branches '"+node.getAttributeValue("name")+"' has not been found on github");
                        }
                    }
                    if(racine.getName().equals("Configuration") && node.getChildren().size()>0){
                        plugins.get(node.getName()).setConfigFile(node);
                    }
                    plugins.get(node.getName()).construct(importer.getFeatureModelFor(node.getAttribute("name").getValue()),
                                importer.getImport());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //TODO: make preprocessor juste befor compile
        System.out.println("configuration assemblée");
        //compileManager.getInstance().maven_powerShell("temp", "compile");
        System.out.println("compilation effectuée");
        if(withoutTest){
            //compileManager.getInstance().maven_powerShell("temp", "package -DskipTests");
        }else{
            //compileManager.getInstance().maven_powerShell("temp", "package");
        }
        System.out.println("packagation effectuée");
    }
    public static void checFeatureModel(Element racine){

    }

    /**
     * Load plugin available for prossessing
     * @return Map<String, interpreter> plugin's name and the plugin
     */
    static public Map<String, Interpreter> loadPlugins(){
        ServiceLoader<Interpreter> serviceLoader = ServiceLoader.load(Interpreter.class);
        Map<String, Interpreter> services = new HashMap<>();
        for (Interpreter service : serviceLoader) {
            services.put(service.getName(), service);
        }
        return services;
    }
}
