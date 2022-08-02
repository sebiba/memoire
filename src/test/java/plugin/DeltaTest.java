package plugin;

import lib.JavaFileManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeltaTest {
    @Test
    void test_getName() {
        assertEquals("Delta", new Delta().getName());
    }

    @Test
    void test_checConstruct() {
        Delta test = new Delta();
        assertDoesNotThrow(()->
            assertTrue(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/featureModelOk.xml")
                .getRootElement()
                .getChild("Delta"))));
    }
    @Test
    void test_construct_False() {
        Delta test = new Delta();
        assertDoesNotThrow(()->
            assertFalse(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml")
                .getRootElement())));
    }

    @Test
    void test_checImport() {
    }
}