package plugin;

import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import lib.JavaFileManager;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.io.IOException;
import java.util.*;


@AutoService(interpreter.class)
public class SpringPreprocessor implements interpreter{
    private String file =null;
    private Map<String, String> var = new HashMap<>();
    private Element node=null;
    private Map<String, String> importer = new HashMap<>();
    private final String detector="##";
    @Override
    public String getName() {
        return "SpringPreprocessor";
    }
    @Override
    public boolean checConstruct(Element node) {
        for (Element child:node.getChildren()) {
            if(Objects.equals(child.getName(), "file")){
                this.file = child.getAttributeValue("path");
            } else if (Objects.equals(child.getName(), "var")) {
                for (Attribute attr:child.getAttributes()) {
                    this.var.put(attr.getName(), attr.getValue());
                }
            }
        }
        this.node = node;
        return file != null;
    }
    @Override
    public void checImport(Element node, Map<String, String> importerParam) {
        for (Attribute attr:node.getAttributes()) {
            if(!attr.getValue().equals("")){
                this.importer.put(attr.getName(), attr.getValue());
            }
        }
        if (!importerParam.isEmpty()){
            for (Map.Entry<String, String> attr:importerParam.entrySet()) {
                this.importer.putIfAbsent(attr.getKey(), attr.getValue());
            }
        }
    }
    @Override
    public void getChildren() {
    }
    @Override
    public void getAttributs() {

    }
    @Override
    public void prettyPrint() {
        System.out.println("$$$$$$$$$$$$$$$$$"+this.getName()+"$$$$$$$$$$$$$$$$");
        Map<String, String> map = new HashMap<String, String>();
        for (Element child:this.node.getChildren()) {
            map.put(child.getName(), String.valueOf(child.getAttributes()));
        }
        for (int i = 0; i < map.size(); i++) {
            System.out.print(map.keySet().toArray()[i]+"\t");
            System.out.print(map.values().toArray()[i]+"\n");
        }
        System.out.println(map.toString());
    }

    private int lineNbrNextPreprocessorInList(List<String>lines, String construct, int start) {
        for (int i = start; i < lines.size(); i++) {
            if(lines.get(i).contains(this.detector) && lines.get(i).contains(construct)){
                return i;
            }
        }
        return -1;
    }
    private Boolean IsIfStatementTrue(String line){
        return true;
    }
        @Override
    public void insert() {
        if(lib.JavaFileManager.getInstance().isFileInProjectDirectory(file)){
            List<String> lines = lib.JavaFileManager.getInstance().getFileContentAsLines(file);
            int lineIf = this.lineNbrNextPreprocessorInList(lines, "if", 0);
            int lineElse = this.lineNbrNextPreprocessorInList(lines, "else", lineIf);
            int lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endIf", lineIf);
            //ELSE statement found in file
            if(lineElse!=-1){
                //IF statement is true AND ELSE statement founded
                if(this.IsIfStatementTrue(lines.get(lineIf))){
                    //TODO: remove else statement + if and endif lines
                    lib.JavaFileManager.getInstance().removeOneLine(lineIf,file);//remove if line
                    lib.JavaFileManager.getInstance().removeLines(lineElse,lineEndIf,file);//remove else to endif statement
                }
                //IF statement is false AND ELSE statement founded
                else{
                    //TODO: remove if statement + else and endif line
                    lib.JavaFileManager.getInstance().removeLines(lineIf,lineElse,file);//remove if statement
                    lib.JavaFileManager.getInstance().removeOneLine(lineEndIf,file);//remove endif line
                }
            }
            //no ELSE statement found in file
            else{
                //IF statement is true AND ELSE not founded
                if(this.IsIfStatementTrue(lines.get(lineIf))){
                    //TODO: remove if + endif lines
                    lib.JavaFileManager.getInstance().removeOneLine(lineIf, file);//remove if line
                    lib.JavaFileManager.getInstance().removeOneLine(lineEndIf, file);//remove endif line
                }
                //IF statement is false AND ELSE not founded
                else{
                    //TODO: remove if statement
                    lib.JavaFileManager.getInstance().removeLines(lineIf, lineEndIf, file);//remove if to endif statement
                }

            }
        }
    }
}
