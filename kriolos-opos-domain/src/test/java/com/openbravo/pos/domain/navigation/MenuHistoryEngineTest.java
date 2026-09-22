package com.openbravo.pos.domain.navigation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MenuHistoryEngine")
class MenuHistoryEngineTest {

    private MenuHistoryEngine engine;

    // Deactivation guard helpers
    private static final BooleanSupplierAlias ALLOW = () -> true;
    private static final BooleanSupplierAlias BLOCK = () -> false;

    // Alias to avoid lambda verbosity in assertions
    @FunctionalInterface interface BooleanSupplierAlias extends java.util.function.BooleanSupplier {}

    private static final ScreenRoute SALES    = new ScreenRoute("com.openbravo.pos.sales.JPanelTicket", "Sales");
    private static final ScreenRoute STOCK    = new ScreenRoute("com.openbravo.pos.inventory.StockManagement", "Stock");
    private static final ScreenRoute REPORTS  = new ScreenRoute("com.openbravo.pos.reports.JPanelReports", "Reports");

    @BeforeEach
    void setUp() {
        engine = new MenuHistoryEngine();
    }

    // =========================================================================
    @Nested @DisplayName("navigate()")
    class Navigate {

        @Test @DisplayName("first navigate returns Navigated with canGoBack=false")
        void firstNavigate_noBack() {
            var result = engine.navigate(SALES, ALLOW);
            assertInstanceOf(NavigationResult.Navigated.class, result);
            var nav = (NavigationResult.Navigated) result;
            assertEquals(SALES, nav.target());
            assertFalse(nav.canGoBack());
            assertFalse(nav.canGoForward());
        }

        @Test @DisplayName("second navigate gives canGoBack=true, canGoForward=false")
        void secondNavigate_canGoBack() {
            engine.navigate(SALES, ALLOW);
            var result = engine.navigate(STOCK, ALLOW);
            var nav = (NavigationResult.Navigated) result;
            assertTrue(nav.canGoBack());
            assertFalse(nav.canGoForward());
        }

        @Test @DisplayName("navigating to same route returns AlreadyCurrent")
        void sameRoute_alreadyCurrent() {
            engine.navigate(SALES, ALLOW);
            var result = engine.navigate(SALES, ALLOW);
            assertInstanceOf(NavigationResult.AlreadyCurrent.class, result);
            assertEquals(SALES, ((NavigationResult.AlreadyCurrent) result).current());
        }

        @Test @DisplayName("blocked guard returns Blocked, state unchanged")
        void blockedGuard_stateUnchanged() {
            engine.navigate(SALES, ALLOW);
            var result = engine.navigate(STOCK, BLOCK);
            assertInstanceOf(NavigationResult.Blocked.class, result);
            assertEquals(SALES, engine.current());
            assertFalse(engine.canGoBack()); // SALES is first; still no back
        }

        @Test @DisplayName("new navigate after back clears forward stack")
        void navigate_clearsForwardStack() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            engine.back(ALLOW);             // back to SALES, STOCK in forward
            engine.navigate(REPORTS, ALLOW);// new nav — forward must clear
            assertFalse(engine.canGoForward());
        }
    }

    // =========================================================================
    @Nested @DisplayName("back()")
    class Back {

        @Test @DisplayName("back on empty history returns NoHistory BACK")
        void emptyBack_noHistory() {
            var result = engine.back(ALLOW);
            assertInstanceOf(NavigationResult.NoHistory.class, result);
            assertEquals(NavigationResult.Direction.BACK,
                    ((NavigationResult.NoHistory) result).direction());
        }

        @Test @DisplayName("back returns previous route and enables forward")
        void back_returnsCorrectRoute() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            var result = engine.back(ALLOW);
            var nav = (NavigationResult.Navigated) result;
            assertEquals(SALES, nav.target());
            assertTrue(nav.canGoForward());
            assertFalse(nav.canGoBack());
        }

        @Test @DisplayName("back blocked by guard leaves state intact")
        void back_blocked() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            var result = engine.back(BLOCK);
            assertInstanceOf(NavigationResult.Blocked.class, result);
            assertEquals(STOCK, engine.current());
        }
    }

    // =========================================================================
    @Nested @DisplayName("forward()")
    class Forward {

        @Test @DisplayName("forward on empty stack returns NoHistory FORWARD")
        void emptyForward_noHistory() {
            var result = engine.forward(ALLOW);
            assertInstanceOf(NavigationResult.NoHistory.class, result);
            assertEquals(NavigationResult.Direction.FORWARD,
                    ((NavigationResult.NoHistory) result).direction());
        }

        @Test @DisplayName("forward after back restores route")
        void forward_restoresRoute() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            engine.back(ALLOW);
            var result = engine.forward(ALLOW);
            var nav = (NavigationResult.Navigated) result;
            assertEquals(STOCK, nav.target());
            assertFalse(nav.canGoForward());
            assertTrue(nav.canGoBack());
        }

        @Test @DisplayName("forward blocked leaves state intact")
        void forward_blocked() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            engine.back(ALLOW);
            engine.forward(BLOCK);
            assertEquals(SALES, engine.current());
        }
    }

    // =========================================================================
    @Nested @DisplayName("reset()")
    class Reset {

        @Test @DisplayName("reset clears all history and resets to NULL_ROUTE")
        void reset_clearsAll() {
            engine.navigate(SALES, ALLOW);
            engine.navigate(STOCK, ALLOW);
            engine.reset();
            assertEquals(ScreenRoute.NULL_ROUTE, engine.current());
            assertFalse(engine.canGoBack());
            assertFalse(engine.canGoForward());
        }
    }

    // =========================================================================
    @Nested @DisplayName("ScreenRoute validation")
    class ScreenRouteValidation {

        @Test @DisplayName("blank taskClass throws IllegalArgumentException")
        void blankTaskClass_throws() {
            assertThrows(IllegalArgumentException.class, () -> new ScreenRoute("  ", "Title"));
        }

        @Test @DisplayName("null taskClass throws IllegalArgumentException")
        void nullTaskClass_throws() {
            assertThrows(IllegalArgumentException.class, () -> new ScreenRoute(null, "Title"));
        }

        @Test @DisplayName("null title normalises to empty string")
        void nullTitle_normalisedToEmpty() {
            var route = new ScreenRoute("com.example.Panel", null);
            assertEquals("", route.title());
        }
    }
}
