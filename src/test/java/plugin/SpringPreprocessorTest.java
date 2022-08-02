package plugin;

import lib.JavaFileManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class SpringPreprocessorTest {

    @Test
    void test_getName() {
        assertEquals("SpringPreprocessor", new SpringPreprocessor().getName());
    }

    @Test
    void test_checConstruct() {
        SpringPreprocessor test = new SpringPreprocessor();
        assertDoesNotThrow(()->
            assertTrue(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/featureModelOK.xml")
                .getRootElement()
                .getChild("SpringPreprocessor"))));
    }
    @Test
    void test_checConstruct_BAD() {
        SpringPreprocessor test = new SpringPreprocessor();
        assertDoesNotThrow( ()->
            assertFalse(test.checConstruct(JavaFileManager.getInstance()
                .getXmlFile("src/test/resources/featureModelBAD.xml")
                .getRootElement())));
    }
}