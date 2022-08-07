package plugin;

import com.google.auto.service.AutoService;
import interfaces.Interpreter;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;


@AutoService(Interpreter.class)
public class Plugin implements Interpreter {
    private String remote =null;

    @Override
    public String getName() {return "Plugin";}
    @Override
    public String getxsdDeclaration() throws IOException {
        return Files.readString(Path.of("src/main/resources/plugin.xsd.txt"), StandardCharsets.UTF_8);
    }
    @Override
    public void construct(Element node, Map<String, String> importer) {
        this.remote = node.getAttributeValue("url");
        if(Objects.equals(this.remote, "") || this.remote == null){
            if(new Importer().isAnUrl(importer.values().toArray()[0].toString())){
                List<String> path = List.of(new File(String.valueOf(importer.values().toArray()[0])).getParent()
                    .split(Pattern.quote(System.getProperty("file.separator"))));
                this.remote = String.join("/",path.subList(0, path.size()-1))+"/tree/"+node.getAttributeValue("name");
            }else{
                this.remote = node.getAttributeValue("name");
            }
        }
        for (Element child : node.getChildren()) {
            if (Objects.equals(child.getName(), "file")) {
                try {
                    this.checImport(new File(importer.keySet().toArray()[0].toString()).getParent(),
                                    importer,
                                    child.getAttributeValue("path"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void setConfigFile(Element featureModel) {

    }

    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) throws IOException {
        List<String> path = List.of(new File(String.valueOf(importer.values().toArray()[0])).getParent()
            .split(Pattern.quote(System.getProperty("file.separator"))));
        String director = String.join("\\",path.subList(0, path.size()-1));
        if(!JavaFileManager.getInstance().isFileInProjectDirectory(file)) {
            if(!this.remote.endsWith("\\") && !file.startsWith("\\")){
                file = "\\".concat(file);
            }
            if(new Importer().isAnUrl(this.remote)){
                JavaFileManager.getInstance().downloadFileFromGitTo(this.remote+file, localDirect+file);
            }else{
                JavaFileManager.getInstance().copyFileFrom(director+"\\"+this.remote+file, localDirect+file);
            }
        }
    }
}
