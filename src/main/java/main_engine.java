import exceptions.RequirementException;
import exceptions.StructureNotSupportedException;
import lib.CompileManager;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class main_engine {

    private static final Logger logger = LogManager.getLogger(main_engine.class);
    public static void main(String[] args) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        File Config= new File("configLog4j.xml");
        context.setConfigLocation(Config.toURI());
        if(args.length == 0){
            showHelp();
            return;
        }
        Document document = null;
        try {
            document = JavaFileManager.getInstance().getXmlFile(args[0]);
        } catch (IOException | JDOMException e) {
            logger.error("le fichier de configuration spécifié est introuvable");
            return;
        }
        assert document != null;
        Element racine = document.getRootElement();
        switch (racine.getName()){
            case "Configuration":
                try {
                    buildConfig(racine, args.length > 1 && Boolean.parseBoolean(args[1]));
                }catch (RequirementException | StructureNotSupportedException e){
                    logger.error(e);
                    return;
                }
                break;
            case "FeatureModel":
                try {
                    checFeatureModel(racine, args[0]);
                    System.out.println(args[0]+ " has a supported structure.");
                } catch (StructureNotSupportedException | NoSuchFieldException | IOException | SAXException e) {
                    logger.error(e);
                    return;
                }
                break;
            default:
                logger.warn(args[0]+ " has wrong root element");
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
     * @param configRacine root xml element from the xml file
     */
    public static void buildConfig(Element configRacine, Boolean withoutTest) throws RequirementException, StructureNotSupportedException {
        Importer importer = new Importer(configRacine);
        List<String> gitBranches = new ArrayList<>();
        if(importer.isSourceGitRepo()){
            String url = importer.getRemoteImport().substring(0,importer.getRemoteImport().lastIndexOf("/"));
            gitBranches = JavaFileManager.getInstance().getBranchesFromGitRepo(url);
        }
        try {
            if(!importer.checSelection(configRacine)){
                throw new RequirementException("Erreur lors de la verification des requirements");
            }
            variantAdder(configRacine, importer, gitBranches);
        } catch (IOException | StructureNotSupportedException | JDOMException | RequirementException e) {
            logger.error(e);
            return;
        }
        System.out.println("configuration assemblée");
        CompileManager.getInstance().maven_powerShell("build", "compile");
        System.out.println("compilation effectuée");
        if(withoutTest){
            CompileManager.getInstance().maven_powerShell("build", "package -DskipTests");
        }else{
            CompileManager.getInstance().maven_powerShell("build", "package");
        }
        System.out.println("packagation effectuée");
    }
    public static void variantAdder(Element configRacine, Importer importer, List<String> gitBranches) throws StructureNotSupportedException, IOException, JDOMException {
        Map<String, Interpreter> pluginsList = loadPlugins();
        if(pluginsList.size() == 0){
            System.out.println("Aucun plugins a pu être chargé");
            return;
        }
        for (Element node: configRacine.getChildren()) {
            //load import attr in map to pass to each variant
            if(!node.getName().equals("import")){
                //apply every plugin to corresponding variant
                if(pluginsList.get(node.getName()) == null){
                    throw new RuntimeException("le variant: "+node.getAttributeValue("name")+" utilise un type de connexion inconu: "+node.getName());
                }
                System.out.printf("%-30s%s%n", "Ajout du variant: "+node.getAttributeValue("name"),"de type:"+node.getName());
                if(importer.isSourceGitRepo()){
                    if(!gitBranches.contains(node.getAttributeValue("name")) && !gitBranches.isEmpty()){
                        throw new StructureNotSupportedException("The branches '"+node.getAttributeValue("name")+"' has not been found on github");
                    }
                }
                if(configRacine.getName().equals("Configuration") && node.getChildren().size()>0){
                    pluginsList.get(node.getName()).setConfigFile(node);
                }
                pluginsList.get(node.getName()).construct(importer.getFeatureModelFor(node.getAttribute("name").getValue()),
                    importer.getImport());
            }
        }
    }
    public static void checFeatureModel(Element racine, String featureModelPath) throws StructureNotSupportedException, NoSuchFieldException, IOException, SAXException {
        Map<String, Interpreter> plugins = loadPlugins();
        String xsdFile = "src/main/resources/used.xsd";
        JavaFileManager.getInstance().copyFileFrom("src/main/resources/base.xsd", xsdFile);
        /*for (Element variant:racine.getChildren()) {
            if(!plugins.containsKey(variant.getName())){
                throw new NoSuchFieldException("The featureModel call a plugin called \""+variant.getName()+"\" but no plugin with that name is registred");
            }
            else if(!plugins.get(variant.getName()).checConstruct(variant)){
                throw new StructureNotSupportedException("the variant \""+variant.getName()+"\" with the name \""+
                variant.getAttributeValue("name")+"\" has an unsupported structure.");
            }
        }*/
        List<String> variantName = new ArrayList<>();
        for (Element variant:racine.getChildren()) {
            if(!plugins.containsKey(variant.getName())){
                throw new NoSuchFieldException("The featureModel call a plugin called \""+variant.getName()+"\" but no plugin with that name is registred");
            }
            variantName.add(variant.getName());
        }
        variantName.stream().distinct().forEach(name->{
            try {
                JavaFileManager.getInstance().buildXSDFile(plugins.get(name).getxsdDeclaration(),xsdFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            JavaFileManager.getInstance().applyXSD(xsdFile, featureModelPath);
        } finally {
            new File(xsdFile).delete();
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
        System.out.println("nombre de plugins chargé: "+services.size());
        return services;*/
    }
}
