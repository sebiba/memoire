package plugin;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class importer {
    private String path=null;
    private Boolean isUri=true;
    private final String directory = "temp";

    public importer(){}
    public importer(Element node) {
        List<Attribute> attr = node.getAttributes();
        if(attr.size()>0){
            this.path = attr.get(0).getValue();
            this.isUri = attr.get(0).getName().equalsIgnoreCase("uri");
        }
    }
    public void load() {
        this.copy(this.path, this.directory);
    }
    public void loadFile(String path, String relativePath){
        if(!relativePath.startsWith("\\") && !path.endsWith("\\")){
            relativePath = "\\"+relativePath;
        }
        this.copy(path+relativePath,this.directory+relativePath);
    }
    public void copy(String path, String destination){
        File srcDir = new File(path);
        File destDir = new File(destination);
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            try {
                FileUtils.copyFile(srcDir, destDir);
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
    public String getDirectory(){
        return this.directory;
    }
    public String getName() {
        return "import";
    }
    public void prettyPrint() {
        System.out.println("import: "+this.path+" as "+(this.isUri.equals(true)?"uri":"url"));
    }
}
