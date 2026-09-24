/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.openbravo.pos.ui.api;

import javax.swing.UIManager;

/**
 *
 * @author dev
 */
public class KriolosOposUiApi {

    public static void main(String[] args) {
        // 1. Print all custom theme factories and all internal themes they manage
        System.out.println("=== Custom POS Theme Factories ===");
        System.out.println("Total factories loaded: " + POSThemeManager.getAllPOSThemeFactory().size());
        System.out.println("----------------------------------------");
        
        POSThemeManager.getAllPOSThemeFactory().forEach(factory -> {
            System.out.println("Factory ID: " + factory.getClass().getName());
            System.out.println("Factory Name: " + factory.getClass().getName());
            System.out.println("Available Themes inside this factory:");
            
            // Iterates and prints all themes registered inside the current factory
            if (factory.getAvailableThemes() != null && !factory.getAvailableThemes().isEmpty()) {
                factory.getAvailableThemes().forEach(theme -> {
                    System.out.println("  -> Theme ID: " + theme.id());
                    System.out.println("     Theme Name: " + theme.name());
                    System.out.println("     Theme Mode: " + theme.mode());
                });
            } else {
                System.out.println("  (No individual themes declared inside this factory)");
            }
            System.out.println("----------------------------------------");
        });
        
        System.out.println();

        // 2. Print all natively installed Look and Feel (Laf) engines in this JVM environment
        System.out.println("=== Installed Swing Look and Feel (Laf) ===");
        UIManager.LookAndFeelInfo[] installedLafs = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lafInfo : installedLafs) {
            System.out.println("Name: " + lafInfo.getName());
            System.out.println("Class: " + lafInfo.getClassName());
            System.out.println("----------------------------------------");
        }
    }
}
