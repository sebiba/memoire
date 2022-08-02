import exceptions.StructureNotSupportedException;
import lib.Importer;
import lib.JavaFileManager;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class libTest {
    public void deleteAllFileFrom(String path){
        File[] allContents = new File(path).listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteAllFileFrom(file.getPath());
            }
        }
        new File(path).delete();
    }
    //region Importer
    @Test
    public void test_ImporterConstructor(){
        assertDoesNotThrow(()->{
            Importer importer = new Importer(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml").getRootElement());
            assertEquals("C:\\unamur\\Master\\memoire\\SPL\\main\\featureModel.xml", importer.getRemoteImport());
            assertEquals("build\\featureModel.xml", importer.getLocalImport());
            assertFalse(importer.isSourceGitRepo());
            deleteAllFileFrom("build");
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
        assertFalse(new Importer().isAnUrl("coucou.ceciEstUnTest.memoire.unamur@java"));
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
            importer.setLocalImport("src/test/resources");//to look for featureModelBad.xml to test
            assertFalse(importer.checSelection(racine));
            deleteAllFileFrom("build");
        });
    }

    //endregion
}