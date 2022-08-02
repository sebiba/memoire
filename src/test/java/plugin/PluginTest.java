package plugin;

import lib.JavaFileManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginTest {

    @Test
    void test_getName() {
        assertEquals("Plugin", new Plugin().getName());
    }

    @Test
    void test_checConstruct() {
        Plugin test = new Plugin();
        assertDoesNotThrow(()->
            assertTrue(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/featureModelOk.xml")
                .getRootElement()
                .getChild("Plugin"))));
    }
    @Test
    void test_construct_False() {
        Plugin test = new Plugin();
        assertDoesNotThrow(()->
            assertFalse(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml")
                .getRootElement())));
    }

    @Test
    void test_checImport() {
    }
}