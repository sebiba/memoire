package lib;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;

import java.util.HashMap;
import java.util.Map;

public class compileManager {
    private static compileManager instance = null;
    public static compileManager getInstance(){
        if (instance == null){
            instance = new compileManager();
        }
        return instance;
    }

    public void maven_powerShell(String path, String command) throws PowerShellNotAvailableException {
        /*final ClassWorld classWorld = new ClassWorld("plexus.core", getClass().getClassLoader());
        MavenCli cli = new MavenCli(classWorld);
        System.setProperty("maven.multiModuleProjectDirectory", path);
        int result = cli.doMain(new String[] { command },
                                path,
                                System.out, System.out);
        System.out.println("\n\nresult: " + result);*/
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("cd "+path);
        /*Map<String, String> myConfig = new HashMap<>();
        myConfig.put("maxWait", "300000");
        powerShell.configuration(myConfig);*/
        response = powerShell.executeCommand("mvn "+command);
        powerShell.close();
        System.out.println(response.getCommandOutput());
    }
}
