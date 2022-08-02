import exceptions.StructureNotSupportedException;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class libTest {
    static final String path = "build";
    public void deleteAllFileFrom(String path){
        File[] allContents = new File(path).listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteAllFileFrom(file.getPath());
            }
        }
        new File(path).delete();
    }
    @AfterEach
    public void deleteBuild(){
        deleteAllFileFrom(path);
    }
    //region Importer
    @Test
    public void test_setRemoteImport(){
        Importer test = new Importer();
        test.setRemoteImport("test");
        assertEquals("test", test.getRemoteImport());
    }
    @Test
    public void test_setLocalImport(){
        Importer test = new Importer();
        test.setLocalImport("test");
        assertEquals("test", test.getLocalImport());
    }

    @Test
    public void test_getFeatureModelFor(){
        Importer test = new Importer();
        test.setLocalImport("src/test/resources/featureModelOK.xml");
        Element expected;
        try {
            Element racine = JavaFileManager.getInstance()
                                        .getXmlFile("src/test/resources/featureModelOK.xml")
                                        .getRootElement();
            expected = racine.getChildren().stream().filter(
                x->x.getAttributeValue("name").equals("logging")
            ).collect(Collectors.toList()).get(0);
        } catch (IOException | JDOMException e) {
            throw new RuntimeException(e);
        }
        new XMLOutputter().outputString(expected);
        assertDoesNotThrow(()->{
            String result = new XMLOutputter().outputString(test.getFeatureModelFor("logging"));
            assertEquals(new XMLOutputter().outputString(expected), result);
        });
    }

    @Test
    public void test_getImport(){
        Importer test = new Importer();
        test.setLocalImport("localImport");
        test.setRemoteImport("RemoteImport");
        Map<String, String> expected= new HashMap<>();
        expected.put("localImport", "RemoteImport");
        assertEquals(expected,test.getImport());
    }
    @Test
    public void test_ImporterConstructor(){
        assertDoesNotThrow(()->{
            Importer importer = new Importer(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml").getRootElement());
            assertEquals("C:\\unamur\\Master\\memoire\\SPL\\main\\featureModel.xml", importer.getRemoteImport());
            assertEquals("build\\featureModel.xml", importer.getLocalImport());
            assertFalse(importer.isSourceGitRepo());
        });
    }
    @Test
    public void test_ImporterConstructorBadRacine(){
        assertThrows(StructureNotSupportedException.class,()->{
            Importer importer = new Importer(new Element("test"));
        });
    }

    @Test
    public void test_isAnUrl(){
        assertTrue(new Importer().isAnUrl("www.google.be"));
    }
    @Test
    public void test_isAnUrl2(){
        assertTrue(new Importer().isAnUrl("https://www.github.com"));
    }
    @Test
    public void test_isAnUrl3(){
        assertTrue(new Importer().isAnUrl("https://github.com/sebiba/memoirePOC.git"));
    }
    @Test
    public void test_isAnUrlFalse(){
        assertFalse(new Importer().isAnUrl("coucou.ceciEstUnTest.memoire.unamur@[java]**@@@@@"));
    }
    @Test
    public void test_isAnUrlEmpty(){
        assertFalse(new Importer().isAnUrl(""));
    }

    @Test
    public void test_selectionFalse(){
        assertDoesNotThrow(()->{
            Element racine = JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml").getRootElement();
            Importer importer = new Importer(racine);
            importer.setLocalImport("src/test/resources/featureModelBAD.xml");//to look for featureModelBad.xml to test
            assertFalse(importer.checSelection(racine));
        });
    }
    @Test
    public void test_selection(){
        assertDoesNotThrow(()->{
            Element racine = JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml").getRootElement();
            Importer importer = new Importer(racine);
            importer.setLocalImport("src/test/resources/featureModelOK.xml");//to look for featureModelBad.xml to test
            assertTrue(importer.checSelection(racine));
        });
    }
    //endregion
    //region JavaFileManager
    @Test
    public void test_isFileInProjectDirectory(){
        try {
            File myObj = new File(path+"/test.txt");
            File directory = new File(path);
            if (! directory.exists()){
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        assertTrue(JavaFileManager.getInstance().isFileInProjectDirectory("test.txt"));
    }
    @Test
    public void test_isFileInProjectDirectory_False(){
        try {
            File myObj = new File(path+"/test.txt");
            File directory = new File(path);
            if (! directory.exists()){
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        assertFalse(JavaFileManager.getInstance().isFileInProjectDirectory("java.txt"));
    }
    @Test
    public void test_isFileInProjectDirectory_NoDirectory(){
        assertThrows(IllegalArgumentException.class, ()->
            JavaFileManager.getInstance().isFileInProjectDirectory("test.txt")
        );
    }

    @Test
    public void test_deleteFile(){
        try {
            File myObj = new File(path+"/test.txt");
            File directory = new File(path);
            if (! directory.exists()){
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        JavaFileManager.getInstance().deleteFile("test.txt");
        assertFalse(new File(path+"/test.txt").exists());
    }
    @Test
    public void test_deleteFile_NoFile(){
        try {
            File myObj = new File(path+"/test.txt");
            File directory = new File(path);
            if (! directory.exists()){
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        JavaFileManager.getInstance().deleteFile("java.txt");
        assertTrue(new File(path+"/test.txt").exists());
    }

    @Test
    public void test_getFileContentAsLines() {
        try {
            File myObj = new File(path + "/test.txt");
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
                FileWriter writer = new FileWriter(path+"/test.txt");
                writer.write("test");
                writer.close();
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Assertions.assertDoesNotThrow( ()->{
            assertEquals(new ArrayList<String>(){{add("test");}},
                JavaFileManager.getInstance().getFileContentAsLines("test.txt"));
        });
    }
    @Test
    public void test_getFileContentAsLines_emptyFile() {
        try {
            File myObj = new File(path + "/test.txt");
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertDoesNotThrow(()->{
            assertEquals(new ArrayList<String>(), JavaFileManager.getInstance().getFileContentAsLines("test.txt"));
        });
    }
    @Test
    public void test_getFileContentAsLines_noFile() {
        assertThrows(FileNotFoundException.class, ()->{
            JavaFileManager.getInstance().getFileContentAsLines("test.txt");
        });
    }
    @Test
    public void test_saveListInFile(){
        try {
            File myObj = new File(path + "/test.txt");
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<String> testArray = new ArrayList<>() {{
            add("test");
            add("java");
            add("memoire");
        }};
        assertDoesNotThrow(()->{
            JavaFileManager.getInstance().saveListInFile("test.txt", testArray);
            assertEquals(testArray, JavaFileManager.getInstance().getFileContentAsLines("test.txt"));
        });
    }
    @Test
    public void test_saveListInFile_noFile(){
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
        List<String> testArray = new ArrayList<>() {{
            add("test");
            add("java");
            add("memoire");
        }};
        assertDoesNotThrow(()-> {
            JavaFileManager.getInstance().saveListInFile("test.txt", testArray);
            assertEquals(testArray, JavaFileManager.getInstance().getFileContentAsLines("test.txt"));
        });
    }
    @Test
    public void test_saveListInFile_emptyArray(){
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdir();
        }
        List<String> testArray = new ArrayList<>();
        assertDoesNotThrow(()->{
            JavaFileManager.getInstance().saveListInFile("test.txt", testArray);
            assertEquals(testArray, JavaFileManager.getInstance().getFileContentAsLines("test.txt"));
        });
    }
    @Test
    public void test_saveListInFile_noDirectory(){
        List<String> testArray = new ArrayList<>();
        assertThrows(IOException.class,()->{
            JavaFileManager.getInstance().saveListInFile("test.txt", testArray);
        });
        assertThrows(FileNotFoundException.class,()->{
            JavaFileManager.getInstance().getFileContentAsLines("test.txt");
        });
    }

    @Test
    public void test_copyFileFrom(){
        try {
            File myObj = new File(path + "/test.txt");
            File directory = new File(path);
            if (!directory.exists()) {
                directory.mkdir();
            }
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertDoesNotThrow(()->{
            JavaFileManager.getInstance().copyFileFrom("build/test.txt", "build/java.txt");
        });
        assertTrue(JavaFileManager.getInstance().isFileInProjectDirectory("test.txt"));
        assertTrue(JavaFileManager.getInstance().isFileInProjectDirectory("java.txt"));
    }
    @Test
    public void test_copyFileFrom_noFile(){
        assertThrows(FileNotFoundException.class,()->{
            JavaFileManager.getInstance().copyFileFrom("build/test.txt", "build/java.txt");
        });
        assertThrows(IllegalArgumentException.class,()->{// directory build not created
            JavaFileManager.getInstance().isFileInProjectDirectory("java.txt");
        });
    }

    @Test
    public void test_getXmlFile(){
        assertDoesNotThrow(()->{
            Element test = JavaFileManager.getInstance().getXmlFile("src/test/resources/configTest2.xml").getRootElement();
            assertEquals("Configuration", test.getName());
            assertEquals(2, test.getChildren().size());
            //first child import
            assertEquals("import", test.getChildren().get(0).getName());
            assertEquals(1, test.getChildren().get(0).getAttributes().size());
            assertEquals("C:\\unamur\\Master\\memoire\\SPL\\main\\featureModel.xml", test.getChildren().get(0).getAttributeValue("uri"));
            //second child SpringAspect
            assertEquals("SpringAspect", test.getChildren().get(1).getName());
            assertEquals(1, test.getChildren().get(1).getAttributes().size());
            assertEquals("logging", test.getChildren().get(1).getAttributeValue("name"));
        });
    }
    @Test
    public void test_getXmlFile_noFile(){
        assertThrows(FileNotFoundException.class,()->{
            Element test = JavaFileManager.getInstance().getXmlFile("noFile.xml").getRootElement();
        });
    }

        //endregion
}