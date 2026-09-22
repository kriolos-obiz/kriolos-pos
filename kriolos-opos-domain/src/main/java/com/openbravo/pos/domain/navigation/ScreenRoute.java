//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
package com.openbravo.pos.domain.navigation;

/**
 * Immutable value object representing a navigable screen destination.
 * The {@code taskClass} is the canonical fully-qualified class name used
 * as the CardLayout card key and the ViewManager lookup key.
 *
 * <p>Belongs to: {@code com.openbravo.pos.domain.navigation} bounded context.</p>
 */
public record ScreenRoute(String taskClass, String title) {

    public ScreenRoute {
        if (taskClass == null || taskClass.isBlank()) {
            throw new IllegalArgumentException("taskClass must not be blank");
        }
        title = (title == null) ? "" : title.strip();
    }

    /** Sentinel representing the initial empty/null screen. */
    public static final ScreenRoute NULL_ROUTE = new ScreenRoute("<NULL>", "");

    public boolean isNullRoute() {
        return NULL_ROUTE.taskClass().equals(taskClass);
    }
}
