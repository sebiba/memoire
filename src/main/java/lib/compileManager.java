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
        PowerShell powerShell = PowerShell.openSession();
        PowerShellResponse response = powerShell.executeCommand("cd "+path);
        response = powerShell.executeCommand("mvn "+command);
        powerShell.close();
        System.out.println(response.getCommandOutput());
    }
}
