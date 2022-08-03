package plugin;

import Interpreter;
import com.google.auto.service.AutoService;
import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;


@AutoService(Interpreter.class)
public class SpringPreprocessor implements Interpreter {
    private String file =null;
    private final String detector="##";
    private final Map<String, String> configVar=new HashMap<>();
    @Override
    public String getName() {
        return "SpringPreprocessor";
    }
    @Override
    public boolean checConstruct(Element node) {
        boolean hasvar = false;
        for (Element child:node.getChildren()) {
            if(Objects.equals(child.getName(), "file")){
                this.file = child.getAttributeValue("path");
            }
            if(Objects.equals(child.getName(), "var")){
                hasvar = true;
            }
        }
        return file != null && hasvar;
    }
    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) {

    }
    @Override
    public void construct(Element node, Map<String, String> importer) {
        for (Element file:node.getChildren()) {
            try {
                if(lib.JavaFileManager.getInstance().isFileInProjectDirectory(file.getAttributeValue("path"))){
                    List<String> lines = lib.JavaFileManager.getInstance().getFileContentAsLines(file.getAttributeValue("path"));
                    List<Element> varList = file.getChildren("var");
                    Map<String, String> fileVar = new HashMap<>();
                    for (Element variable:varList) {
                        variable.getAttributes().forEach(a->fileVar.put(a.getName(), a.getValue()));
                    }
                    int lineStart;
                    while(true) {
                        int lineIf=0;
                        int lineElse=-1;
                        do{
                            if(fileVar.keySet().stream().noneMatch(lines.get(lineIf)::contains))lineStart=lineIf+1;
                            else {
                                lineStart = 0;
                            }
                            lineIf = this.lineNbrNextPreprocessorInList(lines, "if", lineStart);
                            if(lineIf==-1) break;
                        }while(!(lineIf >-1) || fileVar.keySet().stream().noneMatch(lines.get(lineIf)::contains));
                        if(lineIf<0){
                            break;
                        }
                        lineElse = this.lineNbrNextPreprocessorInList(lines, "else", lineIf);
                        int lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endif", lineIf);
                        this.directiveHandler(lineIf, lineElse, lineEndIf, file, lines);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public void setConfigFile(Element node){
        for (Element var:node.getChildren()) {
            List<Attribute> attr= var.getAttributes();
            for (Attribute a:attr) {
                this.configVar.put(a.getName(), a.getValue());
            }
        }
    }

    private void directiveHandler(int lineIf, int lineElse, int lineEndIf, Element file, List<String>lines) throws ScriptException, IOException {
        //ELSE statement found in file
        if (lineElse != -1 && lineElse < lineEndIf) {
            //IF statement is true AND ELSE statement founded
            if (this.IsIfStatementTrue(lines.get(lineIf))) {
                lines.remove(lineIf);//remove if line
                lines = this.removeLinesInListFromTo(lineIf, "else", "endif", lines);//remove else statement
                lib.JavaFileManager.getInstance().saveListInFile(file.getAttributeValue("path"), lines);
            }
            //IF statement is false AND ELSE statement founded
            else {
                lines = (this.removeLinesInListFromTo(lineIf, "if", "else", lines));//remove if statement
                lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endif", lineIf);
                lines.remove(lineEndIf);//remove endif line
                lib.JavaFileManager.getInstance().saveListInFile(file.getAttributeValue("path"), lines);
            }
        }
        //no ELSE statement found in file
        else {
            //IF statement is true AND ELSE not founded
            if (this.IsIfStatementTrue(lines.get(lineIf))) {
                lines.remove(lineIf);//remove if line
                lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endif", lineIf);
                lines.remove(lineEndIf);//remove endif line
                lib.JavaFileManager.getInstance().saveListInFile(file.getAttributeValue("path"), lines);
            }
            //IF statement is false AND ELSE not founded
            else {
                lines = (this.removeLinesInListFromTo(lineIf, "if", "endif", lines));//remove if statement
                lib.JavaFileManager.getInstance().saveListInFile(file.getAttributeValue("path"), lines);
            }
        }
    }
    private int lineNbrNextPreprocessorInList(List<String>lines, String construct, int start) {
        for (int i = start; i < lines.size(); i++) {
            if(lines.get(i).contains(this.detector) && lines.get(i).contains(construct)){
                return i;
            }
        }
        return -1;
    }
    private boolean IsIfStatementTrue(String line) throws ScriptException {
        for (Map.Entry<String, String> variable:this.configVar.entrySet()) {
            line = line.replace(variable.getKey(), "\""+variable.getValue()+"\"");
        }
        line = line.replace("##", "");
        line = line.replace("if", "");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        Object result = engine.eval(line);
        return (Boolean) result;
    }
    private List<String> removeLinesInListFromTo(int start, String detector1, String detector2, List<String> lines){
        int lineDetector1 = this.lineNbrNextPreprocessorInList(lines, detector1, start);
        int lineDetector2 = this.lineNbrNextPreprocessorInList(lines, detector2, start);
        //remove else to endif statement
        for (int i = lineDetector1; i <= lineDetector2; i++) {
            if(lines.get(i).contains(detector2) && lines.get(i).contains(this.detector)){
                lines.remove(i);
                break;
            }else{
                lines.remove(i);
                i--;
            }
        }
        return lines;
    }
}















