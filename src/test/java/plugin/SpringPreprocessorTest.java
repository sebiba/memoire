package plugin;

import org.junit.jupiter.api.Test;

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
            assertTrue(test.getxsdDeclaration()));
    }
    @Test
    void test_checConstruct_BAD() {
        SpringPreprocessor test = new SpringPreprocessor();
        assertDoesNotThrow( ()->
            assertFalse(test.getxsdDeclaration()));
    }
}