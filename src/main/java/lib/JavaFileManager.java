package lib;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class JavaFileManager {
    private static JavaFileManager instance = null;
    private static String tempPath = "temp";
    public static JavaFileManager getInstance(){
        if (instance == null){
            instance = new JavaFileManager();
        }
        return instance;
    }
    public boolean isFileInProjectDirectory(String fileName){
        File root = new File(tempPath);
        try {
            Collection<File> files = FileUtils.listFiles(root, null, true);
            for (File o : files) {
                if (o.getName().contains(Paths.get(fileName).getFileName().toString()))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteFile(String path){
        if(!path.startsWith("\\") && !tempPath.endsWith("\\")){
            tempPath = tempPath.concat("\\");
        }
        File delete = new File(tempPath+path);
        return delete.delete();
    }

    public Map<Integer, String> lineWithConstruct(String file, String reg) throws IOException, TypeNotPresentException{
        Map<Integer, String> lineNbr= new TreeMap<>();
        List<String> lines = this.getFileContentAsLines(file);
        for (int i = 0; i < lines.size(); i++) {
            if(lines.get(i).contains(reg)){
                lineNbr.put(i, lines.get(i));
            }
        }
        if(lineNbr.size()==0){
            throw new TypeNotPresentException("the file: "+file+", contain no preprocessor directories", null);
        }
        return lineNbr;
    }

    /**
     * remove lines between start and end limit included from a file
     * @param start first line to remove
     * @param end last line to remove
     * @param path path to the file to process
     */
    public void removeLines(int start, int end, String path) {
        //TODO:remove lines from the file
        List<String> lines = this.getFileContentAsLines(path);
        try {
            FileWriter writer = new FileWriter(tempPath+path);
            for (int i = 0; i < lines.size(); i++) {
                if(i<start || end<i){
                    writer.write(lines.get(i) + System.lineSeparator());
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * remove one line from a file
     * @param lineNbr  line to remove
     * @param path path to the file to process
     */
    public void removeOneLine(int lineNbr, String path){
        List<String> lines = this.getFileContentAsLines(path);
        try {
            FileWriter writer = new FileWriter(tempPath+path);
            for (int i = 0; i < lines.size(); i++) {
                if(i != lineNbr){
                    writer.write(lines.get(i) + System.lineSeparator());
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get file content in list of lines
     * @param path String path to file to read
     * @return List<String> list of lines from the file
     */
    public List<String> getFileContentAsLines(String path){
        if(!path.startsWith("\\") && !tempPath.endsWith("\\")){
            tempPath = tempPath.concat("\\");
        }
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(tempPath+path));
            String line="";
            while(line != null){
                line = reader.readLine();
                if(line!=null){
                    lines.add(line);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
