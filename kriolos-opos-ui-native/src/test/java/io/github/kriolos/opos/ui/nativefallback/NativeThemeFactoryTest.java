package io.github.kriolos.opos.ui.nativefallback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeThemeFactoryTest {

    private NativeThemeFactory factory;

    @BeforeEach
    void setUp() {
        factory = new NativeThemeFactory();
    }

    @Test
    void testHasThemeStandardNative() {
        assertTrue(factory.hasTheme("system"));
        assertTrue(factory.hasTheme("SYSTEM"));
    }

    @Test
    void testHasThemeNullOrUnknown() {
        assertFalse(factory.hasTheme(null));
        assertFalse(factory.hasTheme("non-existent-theme-xyz"));
    }

    @Test
    void testGetAvailableThemes() {
        var themes = factory.getAvailableThemes();
        assertNotNull(themes);
        assertFalse(themes.isEmpty());
        assertTrue(themes.stream().anyMatch(t -> "system".equals(t.id())));
    }
}
