package lib;

import com.profesorfalken.jpowershell.PowerShell;
import com.profesorfalken.jpowershell.PowerShellNotAvailableException;
import com.profesorfalken.jpowershell.PowerShellResponse;

public class CompileManager {
    private static CompileManager instance = null;
    public static CompileManager getInstance(){
        if (instance == null){
            instance = new CompileManager();
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
