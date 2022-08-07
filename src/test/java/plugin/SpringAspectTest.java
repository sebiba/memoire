package plugin;

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
            assertTrue(test.getxsdDeclaration()));
    }
    @Test
    void test_construct_False() {
        SpringAspect test = new SpringAspect();
        assertDoesNotThrow(()->
            assertFalse(test.getxsdDeclaration()));
    }

    @Test
    void test_checImport() {
    }
}