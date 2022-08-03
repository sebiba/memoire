import exceptions.RequirementException;
import exceptions.StructureNotSupportedException;
import lib.CompileManager;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;

public class main_engine {
    public static void main(String[] args) {
        if(args.length == 0){
            showHelp();
            return;
        }
        Document document = null;
        try {
            document = JavaFileManager.getInstance().getXmlFile(args[0]);
        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
        assert document != null;
        Element racine = document.getRootElement();
        switch (racine.getName()){
            case "Configuration":
                try {
                    buildConfig(racine, args.length > 1 && Boolean.parseBoolean(args[1]));
                }catch (RequirementException | StructureNotSupportedException e){
                    e.printStackTrace();
                }
                break;
            case "FeatureModel":
                try {
                    checFeatureModel(racine);
                    System.out.println(args[0]+ " has a supported structure.");
                } catch (StructureNotSupportedException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
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
    public static void buildConfig(Element racine, Boolean withoutTest) throws RequirementException, StructureNotSupportedException {
        Map<String, Interpreter> pluginsList = loadPlugins();
        Importer importer = new Importer(racine);
        List<String> gitBranches = new ArrayList<>();
        if(importer.isSourceGitRepo()){
            String url = importer.getRemoteImport().substring(0,importer.getRemoteImport().lastIndexOf("/"));
            gitBranches = JavaFileManager.getInstance().getBranchesFromGitRepo(url);
        }
        try {
            if(!importer.checSelection(racine)){
                throw new RequirementException("Erreur lors de la verification des requirements");
            }
        } catch (IOException | JDOMException e) {
            throw new RuntimeException(e);
        }
        for (Element node: racine.getChildren()) {
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
                        pluginsList.get(node.getName()).setConfigFile(node);
                    }
                    pluginsList.get(node.getName()).construct(importer.getFeatureModelFor(node.getAttribute("name").getValue()),
                                importer.getImport());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        System.out.println("configuration assemblée");
        CompileManager.getInstance().maven_powerShell("temp", "compile");
        System.out.println("compilation effectuée");
        if(withoutTest){
            CompileManager.getInstance().maven_powerShell("temp", "package -DskipTests");
        }else{
            CompileManager.getInstance().maven_powerShell("temp", "package");
        }
        System.out.println("packagation effectuée");
    }
    public static void checFeatureModel(Element racine) throws StructureNotSupportedException, NoSuchFieldException {
        Map<String, Interpreter> plugins = loadPlugins();
        for (Element variant:racine.getChildren()) {
            if(!plugins.keySet().contains(variant.getName())){
                throw new NoSuchFieldException("The featureModel call a plugin called \""+variant.getName()+"\" but no plugin with that name is registred");
            }
            else if(!plugins.get(variant.getName()).checConstruct(variant)){
                throw new StructureNotSupportedException("the variant \""+variant.getName()+"\" with the name \""+
                variant.getAttributeValue("name")+"\" has an unsupported structure.");
            }
        }
    }

    /**
     * Load plugin available for prossessing
     * @return Map<String, interpreter> plugin's name and the plugin
     */
    static public Map<String, Interpreter> loadPlugins(){
        PluginLoader pluginLoader = new PluginLoader();
        Map<String, Interpreter>pluginsList = new HashMap<>();
        try {
            for (Interpreter plugin:pluginLoader.loadAllPlugins()) {
                pluginsList.put(plugin.getName(),plugin);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return pluginsList;
        /*ServiceLoader<Interpreter> serviceLoader = ServiceLoader.load(Interpreter.class);
        Map<String, Interpreter> services = new HashMap<>();
        for (Interpreter service : serviceLoader) {
            services.put(service.getName(), service);
        }
        return services;*/
    }
}
