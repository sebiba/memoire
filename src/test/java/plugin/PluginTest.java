package plugin;

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
            assertTrue(test.getxsdDeclaration()));
    }
    @Test
    void test_construct_False() {
        Plugin test = new Plugin();
        assertDoesNotThrow(()->
            assertFalse(test.getxsdDeclaration()));
    }

    @Test
    void test_checImport() {
    }
}