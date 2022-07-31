package plugin;

import interfaces.Interpreter;
import com.google.auto.service.AutoService;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Element;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

@AutoService(Interpreter.class)
public class Delta implements Interpreter {
    private String remote=null;

    @Override
    public String getName() {
        return "Delta";
    }
    @Override
    public boolean checConstruct(Element node) {
        String deltaName = node.getAttribute("name").getValue();
        return deltaName != null;
    }

    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) {
        List<String> path = List.of(new File(String.valueOf(importer.values().toArray()[0])).getParent()
            .split(Pattern.quote(System.getProperty("file.separator"))));
        String director = String.join("\\",path.subList(0, path.size()-1));
        if(!JavaFileManager.getInstance().isFileInProjectDirectory(file)) {
            if(!this.remote.startsWith("\\") && !file.endsWith("\\")){
                file = "\\".concat(file);
            }
            //TODO:change as in Aspect.java
            JavaFileManager.getInstance().copyFileFrom(director + "\\" + this.remote + file, localDirect + file);
        }
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
        for (Element cat:node.getChildren()) {
            switch (cat.getName()){
                case "addFile":
                    this.fileAdder(cat, importer);
                    break;
                case "modif":
                    this.fileModif(cat);
                    break;
                case "deleteFile":
                    this.fileDeleter(cat);
                    break;
            }
        }
    }

    @Override
    public void setConfigFile(Element node) {
    }

    private void fileDeleter(Element cat) {
        for (Element file:cat.getChildren()) {
            JavaFileManager.getInstance().deleteFile(file.getAttributeValue("path"));
        }
    }

    private void fileModif(Element cat) {
        for (Element addFile:cat.getChildren()) {
            switch(addFile.getAttributeValue("type")){
                case "java":
                    this.javaAdder(addFile);
                    break;
                case "html":
                    this.htmlAdder(addFile);
                    break;
            }
        }
    }

    private void htmlAdder(Element addFile) {
        List<String> fileContent = JavaFileManager.getInstance().getFileContentAsLines(addFile.getAttributeValue("path"));
        for (Element add:addFile.getChildren()) {
            Document htmlParse = Jsoup.parse(String.join("\n",fileContent));
            Objects.requireNonNull(htmlParse.select("#" + add.getAttributeValue("endOf")).first()).append(add.getText());
            JavaFileManager.getInstance().saveListInFile(addFile.getAttributeValue("path"), Collections.singletonList(htmlParse.toString()));
        }
    }

    private void javaAdder(Element addFile) {
        List<String> fileContent = JavaFileManager.getInstance().getFileContentAsLines(addFile.getAttributeValue("path"));
        //loop from EOF to catch Class end
        /*assomption file end with 1 class end*/
        for (Element add:addFile.getChildren()) {
            for (int i = fileContent.size()-1; i > 0; i--) {
                if(fileContent.get(i).contains("}") && !fileContent.get(i).contains("//")){
                    if(add.getName().equals("add")){
                        fileContent.add(i, add.getText());
                        break;
                    }
                }
            }
        }
        JavaFileManager.getInstance().saveListInFile(addFile.getAttributeValue("path"), fileContent);
    }
    private void fileAdder(Element cat, Map<String, String> importer){
        for (Element file:cat.getChildren()) {
            this.checImport(new File(importer.keySet().toArray()[0].toString()).getParent(),
                            importer,
                            file.getAttributeValue("path"));
        }
    }
}
