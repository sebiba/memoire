package lib;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

public class JavaFileManager {
    private static JavaFileManager instance = null;
    public static JavaFileManager getInstance(){
        if (instance == null){
            instance = new JavaFileManager();
        }
        return instance;
    }
    public boolean isFileInProjectDirectory(String fileName){
        File root = new File("temp");
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
}
