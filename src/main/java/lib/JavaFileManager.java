package lib;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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

    public void saveListInFile(String path, List<String> lines){
        try {
            FileWriter writer = new FileWriter(tempPath+path);
            for (String line : lines) {
                writer.write(line + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFileFrom(String path, String destination){
        File srcDir = new File(path);
        File destDir = new File(destination);
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            try {
                if(srcDir.isFile() && destDir.isDirectory()) {
                    FileUtils.copyFile(srcDir, new File(destDir + "\\" + srcDir.getName()));
                }else{
                    FileUtils.copyFile(srcDir, destDir);
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }

    public Document getXmlFile(String path){
        SAXBuilder sxb = new SAXBuilder();
        Document document = null;
        try {
            document = sxb.build(new File(path));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return document;
    }
}
