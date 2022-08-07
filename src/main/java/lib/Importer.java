package lib;

import exceptions.RequirementException;
import exceptions.StructureNotSupportedException;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Importer {
    private String localImport;
    private String remoteImport;
    private Boolean isGitRepo = false;
    private final String directory = "build";
    public Importer() {

    }
    public Importer(Element node) throws StructureNotSupportedException {
        List<Element> fileImport = node.getChildren().stream().filter(x->x.getName().equals("import")).collect(Collectors.toList());
        if(fileImport.size() == 0){
            throw new StructureNotSupportedException("structure du fichier non supportée.");
        }
        this.importMainFile(fileImport);
        if(!this.isAnUrl(this.remoteImport) && new File(this.remoteImport).isFile())
            this.copy(new File(this.remoteImport).getParent(), this.directory);
    }
    private void importMainFile(List<Element> fileImport){
        for (Element imp:fileImport) {
            for (Attribute attr: imp.getAttributes()) {
                this.copy(attr.getValue(), this.directory);
                if(new File(attr.getValue()).isFile()){
                    this.remoteImport = attr.getValue();
                    this.localImport = this.directory+ "\\" + new File(attr.getValue()).getName();
                }else{
                    this.remoteImport = attr.getValue()+"/FeatureModel.xml";
                    this.localImport = this.directory+"/FeatureModel.xml";
                }
            }
        }
    }
    public void copy(String path, String destination){
        if(this.isAnUrl(path)){
            this.isGitRepo = true;
            JavaFileManager.getInstance().downloadBrancheFromGit(path);
        }else{
            File srcDir = new File(path);
            if(srcDir.isFile() && destination.equals(this.directory)){
                try {
                    FileUtils.copyFile(srcDir, new File(destination + "\\" + srcDir.getName()));
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            File destDir = new File(destination);

            try {
                if(srcDir.isDirectory() && destDir.isDirectory()){
                    FileUtils.copyDirectory(srcDir, destDir);
                }else{
                    if(srcDir.isFile() && destDir.isDirectory()) {
                        FileUtils.copyFile(srcDir, new File(destDir + "\\" + srcDir.getName()));
                    }else{
                        FileUtils.copyFile(srcDir, destDir);
                    }
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
    public boolean isAnUrl(String url){
        final String URL_REGEX =
            "^([-a-zA-Z0-9@:%_\\+.~#?&//=]{2,256}\\.[a-z]{2,4}\\b(\\/[-a-zA-Z0-9@:%_\\+.~#?&//=]*)?)";
        final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
        if (url == null) {
            return false;
        } else if (!(url.startsWith("http")||url.startsWith("ftp"))) {
            url = "http://"+url;
        }

        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }
//region get/set
    public String getDirectory(){
        return this.directory;
    }
    public Element getFeatureModelFor(String name) throws IOException, JDOMException {
        Element feature;
        Document xml = JavaFileManager.getInstance().getXmlFile(this.localImport);
        List<Element> features = xml.getRootElement().getChildren();
        feature = features.stream().filter(x->x.getAttributeValue("name").equals(name)).collect(Collectors.toList()).get(0);
        return feature;
    }
    public String getLocalImport() {
        return localImport;
    }
    public void setLocalImport(String localImport) {
        this.localImport = localImport;
    }
    public String getRemoteImport() {
        return remoteImport;
    }
    public void setRemoteImport(String remoteImport) {
        this.remoteImport = remoteImport;
    }
    public Map<String, String> getImport(){
        Map<String, String> importer = new HashMap<>();
        importer.put(this.localImport, this.remoteImport);
        return importer;
    }
//endregion

    /**
     * go throw featureModel to know required constraints and cchec if constraints are respected
     * @param racine XML Element Configuration
     * @return true if all constraints are respected false otherwise
     * @throws IOException error while opening featureModel's file
     * @throws JDOMException error while parsing XML from featureModel
     */
    public boolean checSelection(Element racine) throws IOException, JDOMException {
        List<Boolean> requires = new ArrayList<>();
        Document featureModel = JavaFileManager.getInstance().getXmlFile(localImport);
        List<String> configVariant = racine.getChildren().stream().map(v->v.getAttributeValue("name")).collect(Collectors.toList());
        for (Element variant: featureModel.getRootElement().getChildren()) {
            if(variant.getAttributeValue("require") != null){
                if(configVariant.contains(variant.getAttributeValue("name"))){
                    configVariant.remove(variant.getAttributeValue("name"));
                    requires.add(this.isVariantSelected(racine, variant.getAttributeValue("require")));
                }
            }
        }
        return !requires.contains(false);
    }

    /**
     * check if a variant of the product line is selected in the configuration.
     * @param racine XML Element Configuration
     * @param require String name of the variant to check
     * @return true if require variant is selected false otherwise
     */
    private Boolean isVariantSelected(Element racine, String require) {
        for (Element variant:racine.getChildren()) {
            if(!variant.getName().equals("import")){
                if(variant.getAttributeValue("name").equals(require)){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean isSourceGitRepo() {
        return isGitRepo;
    }
}
