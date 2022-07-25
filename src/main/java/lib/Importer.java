package lib;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Importer {
    private String localImport;
    private String remoteImport;
    private Boolean isGitRepo = false;
    private final String directory = "build";
    public Importer() {

    }
    public Importer(Element node) {
        List<Element> fileImport = node.getChildren().stream().filter(x->x.getName().equals("import")).collect(Collectors.toList());
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
        if(!this.isAnUrl(this.remoteImport) && new File(this.remoteImport).isFile())
            this.copy(new File(this.remoteImport).getParent(), this.directory);
    }
    public void copy(String path, String destination){
        if(this.isAnUrl(path)){
            try {
                this.isGitRepo = true;
                JavaFileManager.getInstance().downloadBrancheFromGit(path);
            } catch (TransportException e) {
                throw new RuntimeException(e);
            }
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
        try {
            new URL(url).toURI();
            return true;
        }catch (Exception e) {
            return false;
        }
    }
//region get/set
    public String getDirectory(){
        return this.directory;
    }
    public String getName() {
        return "import";
    }
    public Element getFeatureModelFor(String name) {
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
    public boolean checSelection(Element racine) {
        List<Boolean> requires = new ArrayList<>();
        Document featureModel = JavaFileManager.getInstance().getXmlFile(localImport);
        for (Element variant: featureModel.getRootElement().getChildren()) {
            if(variant.getAttributeValue("require") != null){
                requires.add(this.isVariantSelected(racine, variant.getAttributeValue("require")));
            }
        }
        return !requires.contains(false);
    }

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
