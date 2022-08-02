package plugin;

import lib.JavaFileManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpringAspectTest {
    @Test
    void test_getName() {
        assertEquals("SpringAspect", new SpringAspect().getName());
    }

    @Test
    void test_checConstruct() {
        SpringAspect test = new SpringAspect();
        assertDoesNotThrow(()->
            assertTrue(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/featureModelOk.xml")
                .getRootElement()
                .getChild("SpringAspect"))));
    }
    @Test
    void test_construct_False() {
        SpringAspect test = new SpringAspect();
        assertDoesNotThrow(()->
            assertFalse(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/configTest1.xml")
                .getRootElement())));
    }

    @Test
    void test_checImport() {
    }
}