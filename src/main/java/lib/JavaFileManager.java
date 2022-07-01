package lib;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

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
}
