package plugin;

import Interfaces.interpreter;
import com.google.auto.service.AutoService;
import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@AutoService(interpreter.class)
public class SpringPreprocessor implements interpreter{
    private String file =null;
    private final Map<String, String> var = new HashMap<>();
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
        return file != null;
    }

    @Override
    public void checImport(String localDirect, Map<String, String> importer, String file) {

    }

    private int lineNbrNextPreprocessorInList(List<String>lines, String construct, int start) {
        for (int i = start; i < lines.size(); i++) {
            if(lines.get(i).contains(this.detector) && lines.get(i).contains(construct)){
                return i;
            }
        }
        return -1;
    }
    private Boolean IsIfStatementTrue(String line) throws ScriptException {
        for (Map.Entry<String, String> variable:this.var.entrySet()) {
            line = line.replace(variable.getKey(), "\""+variable.getValue()+"\"");
        }
        line = line.replace("##", "");
        line = line.replace("if", "");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        Object result = engine.eval(line);
        return (Boolean) result;
    }

    @Override
    public void construct(Element node, Map<String, String> importer) {
        if(!this.checConstruct(node)) {
            return;
        }
        try {
            if(lib.JavaFileManager.getInstance().isFileInProjectDirectory(file)){
                List<String> lines = lib.JavaFileManager.getInstance().getFileContentAsLines(file);
                int lineIf = this.lineNbrNextPreprocessorInList(lines, "if", 0);
                int lineElse = this.lineNbrNextPreprocessorInList(lines, "else", lineIf);
                int lineEndIf;
                //TODO: chec multiple if in same file
                //ELSE statement found in file
                if(lineElse!=-1){
                    //IF statement is true AND ELSE statement founded
                    if(this.IsIfStatementTrue(lines.get(lineIf))){
                        lines.remove(lineIf);//remove if line
                        lines = (this.removeLinesInListFromTo(lineIf, "else", "endif", lines));//remove else statement
                        lib.JavaFileManager.getInstance().saveListInFile(file, lines);
                    }
                    //IF statement is false AND ELSE statement founded
                    else{
                        lines = (this.removeLinesInListFromTo(lineIf, "if", "else", lines));//remove if statement
                        lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endif", lineIf);
                        lines.remove(lineEndIf);//remove endif line
                        lib.JavaFileManager.getInstance().saveListInFile(file, lines);
                    }
                }
                //no ELSE statement found in file
                else{
                    //IF statement is true AND ELSE not founded
                    if(this.IsIfStatementTrue(lines.get(lineIf))){
                        lines.remove(lineIf);//remove if line
                        lineEndIf = this.lineNbrNextPreprocessorInList(lines, "endif", lineIf);
                        lines.remove(lineEndIf);//remove endif line
                        lib.JavaFileManager.getInstance().saveListInFile(file, lines);
                    }
                    //IF statement is false AND ELSE not founded
                    else{
                        lines = (this.removeLinesInListFromTo(lineIf, "if", "endif", lines));//remove if statement
                        lib.JavaFileManager.getInstance().saveListInFile(file, lines);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public List<String> removeLinesInListFromTo(int start, String detector1, String detector2, List<String> lines){
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
