package plugin;

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
            assertTrue(test.getxsdDeclaration()));
    }
    @Test
    void test_construct_False() {
        Delta test = new Delta();
        assertDoesNotThrow(()->
            assertFalse(test.getxsdDeclaration()));
    }

    @Test
    void test_checImport() {
    }
}