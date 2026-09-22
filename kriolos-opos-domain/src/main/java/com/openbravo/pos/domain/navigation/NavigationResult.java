//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
package com.openbravo.pos.domain.navigation;

/**
 * Sealed result type for all {@link MenuHistoryEngine} operations.
 *
 * <p>The Swing adapter switches on this type using Java 17 pattern matching
 * to decide what UI action to apply — the domain layer never touches Swing.</p>
 *
 * <ul>
 *   <li>{@link Navigated} — navigation succeeded; adapter must show the target route.</li>
 *   <li>{@link AlreadyCurrent} — target is already the active screen; no-op for the adapter.</li>
 *   <li>{@link Blocked} — current screen refused deactivation; adapter may show a warning.</li>
 *   <li>{@link NoHistory} — back/forward requested but the history stack is empty.</li>
 * </ul>
 */
public sealed interface NavigationResult
        permits NavigationResult.Navigated,
                NavigationResult.AlreadyCurrent,
                NavigationResult.Blocked,
                NavigationResult.NoHistory {

    /** Navigation succeeded. Adapter must display {@code target}. */
    record Navigated(ScreenRoute target, boolean canGoBack, boolean canGoForward)
            implements NavigationResult {}

    /** The requested route is already the active one. No UI change needed. */
    record AlreadyCurrent(ScreenRoute current)
            implements NavigationResult {}

    /**
     * The active screen's deactivation guard returned {@code false}.
     * The adapter should inform the user without changing the displayed panel.
     */
    record Blocked(ScreenRoute current)
            implements NavigationResult {}

    /**
     * Back or forward was requested but the respective history stack is empty.
     * The adapter should disable the corresponding button.
     */
    record NoHistory(Direction direction)
            implements NavigationResult {}

    enum Direction { BACK, FORWARD }
}
