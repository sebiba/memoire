package lib;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Importer {
    private String localImport;
    private String remoteImport;
    private final String directory = "temp";
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
                    this.remoteImport = attr.getValue();
                    this.localImport = this.directory;
                }
            }
        }
        this.loadFile(new File(this.remoteImport).getParent(), this.directory);
    }
    public void loadFile(String path, String relativePath){
        this.copy(path,relativePath);
    }
    public void copy(String path, String destination){
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
    public String getDirectory(){
        return this.directory;
    }
    public String getName() {
        return "import";
    }
    public Element getFeatureModelFor(String name) {
        Element feature = null;
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
}
