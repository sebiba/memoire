package lib;

import org.apache.maven.cli.MavenCli;

public class compileManager {
    private static compileManager instance = null;
    public static compileManager getInstance(){
        if (instance == null){
            instance = new compileManager();
        }
        return instance;
    }

    public void maven_exec(String path, String command) {
        MavenCli cli = new MavenCli();
        System.setProperty("maven.multiModuleProjectDirectory", path);
        int result = cli.doMain(new String[] { command },
                                path,
                                System.out, System.out);
        System.out.println("\n\nresult: " + result);
    }
}
