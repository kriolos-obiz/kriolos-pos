//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
package com.openbravo.pos.domain.navigation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Pure, framework-agnostic back/forward navigation history engine.
 *
 * <p><b>Design contract:</b></p>
 * <ul>
 *   <li>Stateful per user session — one instance per {@code JPrincipalApp}.</li>
 *   <li>No Swing, AWT, JDBC, or framework imports.</li>
 *   <li>Thread-safety: single-threaded (EDT) by design; callers must not share across threads.</li>
 *   <li>The deactivation guard ({@link BooleanSupplier}) is injected by the Swing Adapter
 *       so the domain never calls Swing APIs directly.</li>
 * </ul>
 *
 * <p>Belongs to: {@code com.openbravo.pos.domain.navigation} bounded context.</p>
 */
public final class MenuHistoryEngine {

    private static final int MAX_HISTORY = 50;

    private final Deque<ScreenRoute> backStack    = new ArrayDeque<>();
    private final Deque<ScreenRoute> forwardStack = new ArrayDeque<>();
    private ScreenRoute current = ScreenRoute.NULL_ROUTE;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Navigate to a new screen route.
     *
     * <p>If {@code route} equals the current screen, returns {@link NavigationResult.AlreadyCurrent}.
     * Otherwise, the caller-supplied {@code deactivationGuard} is invoked. If it returns
     * {@code false} the current screen refuses to leave and {@link NavigationResult.Blocked}
     * is returned without mutating state.</p>
     *
     * @param route           the target screen — must not be null
     * @param deactivationGuard supplier that calls the current panel's {@code deactivate()} —
     *                          must not be null; the domain never calls Swing directly
     */
    public NavigationResult navigate(ScreenRoute route, BooleanSupplier deactivationGuard) {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(deactivationGuard, "deactivationGuard");

        if (route.equals(current)) {
            return new NavigationResult.AlreadyCurrent(current);
        }

        if (!current.isNullRoute() && !deactivationGuard.getAsBoolean()) {
            return new NavigationResult.Blocked(current);
        }

        if (!current.isNullRoute()) {
            pushBack(current);
        }
        forwardStack.clear();   // new navigation always clears forward history
        current = route;

        return new NavigationResult.Navigated(current, !backStack.isEmpty(), false);
    }

    /**
     * Navigate back one step.
     *
     * @param deactivationGuard called on the current screen before leaving
     */
    public NavigationResult back(BooleanSupplier deactivationGuard) {
        Objects.requireNonNull(deactivationGuard, "deactivationGuard");

        if (backStack.isEmpty()) {
            return new NavigationResult.NoHistory(NavigationResult.Direction.BACK);
        }
        if (!deactivationGuard.getAsBoolean()) {
            return new NavigationResult.Blocked(current);
        }

        forwardStack.push(current);
        current = backStack.pop();

        return new NavigationResult.Navigated(current, !backStack.isEmpty(), !forwardStack.isEmpty());
    }

    /**
     * Navigate forward one step (only valid after one or more {@link #back} calls).
     *
     * @param deactivationGuard called on the current screen before leaving
     */
    public NavigationResult forward(BooleanSupplier deactivationGuard) {
        Objects.requireNonNull(deactivationGuard, "deactivationGuard");

        if (forwardStack.isEmpty()) {
            return new NavigationResult.NoHistory(NavigationResult.Direction.FORWARD);
        }
        if (!deactivationGuard.getAsBoolean()) {
            return new NavigationResult.Blocked(current);
        }

        backStack.push(current);
        current = forwardStack.pop();

        return new NavigationResult.Navigated(current, !backStack.isEmpty(), !forwardStack.isEmpty());
    }

    /** Resets all history and current route. Used on user logout/session end. */
    public void reset() {
        backStack.clear();
        forwardStack.clear();
        current = ScreenRoute.NULL_ROUTE;
    }

    public ScreenRoute current()      { return current; }
    public boolean     canGoBack()    { return !backStack.isEmpty(); }
    public boolean     canGoForward() { return !forwardStack.isEmpty(); }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void pushBack(ScreenRoute route) {
        if (backStack.size() >= MAX_HISTORY) {
            // drop oldest (bottom of deque) to cap memory
            ((ArrayDeque<ScreenRoute>) backStack).removeLast();
        }
        backStack.push(route);
    }
}
