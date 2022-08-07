package lib;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class JavaFileManager {
    private static JavaFileManager instance = null;
    private static String tempPath = "build";
    public static JavaFileManager getInstance(){
        if (instance == null){
            instance = new JavaFileManager();
        }
        return instance;
    }

    /**
     * check if a file is in directory or subdirectory
     * @param fileName String path of the file to look for
     * @return true if the file is found false otherwise
     */
    public boolean isFileInProjectDirectory(String fileName){
        File root = new File(tempPath);
        Collection<File> files = FileUtils.listFiles(root, null, true);
        for (File o : files) {
            if (o.getName().contains(Paths.get(fileName).getFileName().toString()))
                return true;
        }
        return false;
    }
    public void deleteFile(String path){
        if(!path.startsWith("\\") && !tempPath.endsWith("\\")){
            tempPath = tempPath.concat("\\");
        }
        File delete = new File(tempPath+path);
        delete.delete();
    }

    /**
     * get file content in list of lines
     * @param path String path to file to read
     * @return List<String> list of lines from the file
     */
    public List<String> getFileContentAsLines(String path) throws IOException{
        if(!path.startsWith("\\") && !tempPath.endsWith("\\")){
            tempPath = tempPath.concat("\\");
        }
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(tempPath+path));
        String line="";
        while(line != null){
            line = reader.readLine();
            if(line!=null){
                lines.add(line);
            }
        }
        reader.close();
        return lines;
    }

    /**
     * save a list of String in a file create if does not exist
     * @param path String path of the file where to save
     * @param lines List<String> content to save in the file
     * @throws IOException error while writing in the file
     */
    public void saveListInFile(String path, List<String> lines) throws IOException {
        if(!path.startsWith("\\") && !tempPath.endsWith("\\")){
            tempPath = tempPath.concat("\\");
        }
        if(!new File(tempPath+path).exists()){
            new File(tempPath+path).createNewFile();
        }
        FileWriter writer = new FileWriter(tempPath+path);
        for (String line : lines) {
            writer.write(line + System.lineSeparator());
        }
        writer.close();
    }

    /**
     * copy a file from one path to another
     * @param path String where the file start
     * @param destination String where the file end. can be a directory path
     * @throws IOException error while writing the file
     */
    public void copyFileFrom(String path, String destination) throws IOException{
        File srcDir = new File(path);
        File destDir = new File(destination);
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            if(srcDir.isFile() && destDir.isDirectory()) {
                FileUtils.copyFile(srcDir, new File(destDir + "\\" + srcDir.getName()));
            }else{
                FileUtils.copyFile(srcDir, destDir);
            }
        }
    }

    /**
     * get a JDOM XML Document from a xml file's path
     * @param path String path to XML file
     * @return Document containing the XML as a JDOM Element structure
     * @throws IOException error while reading the file
     * @throws JDOMException error while parsing xml
     */
    public Document getXmlFile(String path) throws IOException, JDOMException {
        return  new SAXBuilder().build(new File(path));
    }

    public boolean applyXSD(String xsdPath, String xmlPath) throws IOException, SAXException {
        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new File(xsdPath));
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new File(xmlPath)));
        return true;
    }
    public void buildXSDFile(String content, String file) throws IOException {
        List<String> xsdFile = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line="";
            while(line != null){
                line = reader.readLine();
                if(line!=null){
                    xsdFile.add(line);
                }
            }
        }
        int choiceLine = xsdFile.indexOf("\t<xs:choice maxOccurs=\"unbounded\">");
        xsdFile.add(choiceLine+1, content);
        try(FileWriter writer = new FileWriter(file)){
            for (String line : xsdFile) {
                writer.write(line + System.lineSeparator());
            }
        }
    }


    /**
     * cloning Git branche from url
     * @param url String url from git repository
     */
    public void downloadBrancheFromGit(String url){
        try {
            System.out.println("Cloning "+url+" into "+tempPath);
            Git call = Git.cloneRepository()
                          .setURI(url)
                          .setDirectory(Paths.get(tempPath).toFile())
                          .call();
            call.close();
            System.out.println("Completed Cloning");
        } catch (GitAPIException e) {
            System.out.println("Exception occurred while cloning repo");
            e.printStackTrace();
        } catch (JGitInternalException e){
            //Destination path "build" already exists and is not an empty directory
            if(deleteAllFileFrom(tempPath)){
                this.downloadBrancheFromGit(url);
            }else{
                e.printStackTrace();
            }
        }
    }

    /**
     * download one file from a git repository
     * @param urlGit String url of the file in a specific branche
     * @param path String path to where download the file
     */
    public void downloadFileFromGitTo(String urlGit, String path) {
        URL url = null;
        urlGit = urlGit.replace(":\\", ":\\\\");
        urlGit = urlGit.replace("www.github.com", "raw.githubusercontent.com");
        urlGit = urlGit.replace("github.com", "raw.githubusercontent.com");
        try {
            url = new URL(urlGit);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert url != null;
            URLConnection con = url.openConnection();
            // Show page.
            BufferedReader reader = null;
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            try {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                for (String line; ((line = reader.readLine()) != null);) {
                    out.append(line);
                    out.newLine();
                    System.out.println(line);
                }
            } finally {
                if (reader != null) try { reader.close(); } catch (IOException ignore) {}
                out.flush();
                out.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     *delete all file from a specific folder
     * @param tempPath String path to the folder to delete
     * @return true if succeed false otherwise
     */
    private boolean deleteAllFileFrom(String tempPath) {
        File[] allContents = new File(tempPath).listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteAllFileFrom(file.getPath());
            }
        }
        return new File(tempPath).delete();
    }

    /**
     * get all branches from git repository
     * @param url String url from git repository
     * @return List<String> names of all the existing branches
     */
    public List<String> getBranchesFromGitRepo(String url){
        Collection<Ref> refs;
        List<String> branches = new ArrayList<>();
        try {
            refs = Git.lsRemoteRepository()
                .setHeads(true)
                .setRemote(url)
                .call();
            for (Ref ref : refs) {
                branches.add(ref.getName().substring(ref.getName().lastIndexOf("/")+1));
            }
            Collections.sort(branches);
        } catch (InvalidRemoteException e) {
            System.out.println(" InvalidRemoteException occured in fetchGitBranches "+e);
            e.printStackTrace();
        } catch (TransportException e) {
            System.out.println(" TransportException occurred in fetchGitBranches "+e);
        } catch (GitAPIException e) {
            System.out.println(" GitAPIException occurred in fetchGitBranches "+e);
        }
        return branches;
    }
}
