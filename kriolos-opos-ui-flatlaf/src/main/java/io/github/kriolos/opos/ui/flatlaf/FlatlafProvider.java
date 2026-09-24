/*
 * Copyright (C) 2022 KriolOS
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
package io.github.kriolos.opos.ui.flatlaf;

import java.util.logging.Logger;

/**
 *
 * @author poolborges
 */
public class FlatlafProvider {

    public void getLafInfoList() {

        /*
        lafs.add(new LafInfo("Flat Dark", com.formdev.flatlaf.FlatDarkLaf.class.getCanonicalName()));
        lafs.add(new LafInfo("Flat Darcula", com.formdev.flatlaf.FlatDarculaLaf.class.getCanonicalName()));
        lafs.add(new LafInfo("Flat Light", com.formdev.flatlaf.FlatLightLaf.class.getCanonicalName()));
        lafs.add(new LafInfo("Flat IntelliJ", com.formdev.flatlaf.FlatIntelliJLaf.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc",com.formdev.flatlaf.intellijthemes.FlatArcIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc (Orange)",com.formdev.flatlaf.intellijthemes.FlatArcOrangeIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc Dark",com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc Dark (Orange)",com.formdev.flatlaf.intellijthemes.FlatArcDarkOrangeIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Carbon",com.formdev.flatlaf.intellijthemes.FlatCarbonIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Cobalt 2",com.formdev.flatlaf.intellijthemes.FlatCobalt2IJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Cyan light",com.formdev.flatlaf.intellijthemes.FlatCyanLightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Dark Flat",com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Dark purple",com.formdev.flatlaf.intellijthemes.FlatDarkPurpleIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Dracula",com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gradianto Dark Fuchsia",com.formdev.flatlaf.intellijthemes.FlatGradiantoDarkFuchsiaIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gradianto Deep Ocean",com.formdev.flatlaf.intellijthemes.FlatGradiantoDeepOceanIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gradianto Midnight Blue",com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gradianto Nature Green",com.formdev.flatlaf.intellijthemes.FlatGradiantoNatureGreenIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gray",com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gruvbox Dark Hard",com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkHardIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gruvbox Dark Medium",com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkMediumIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Gruvbox Dark Soft",com.formdev.flatlaf.intellijthemes.FlatGruvboxDarkSoftIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Hiberbee Dark",com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("High contrast",com.formdev.flatlaf.intellijthemes.FlatHighContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Light Flat",com.formdev.flatlaf.intellijthemes.FlatLightFlatIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Design Dark",com.formdev.flatlaf.intellijthemes.FlatMaterialDesignDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Monocai",com.formdev.flatlaf.intellijthemes.FlatMonocaiIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Nord",com.formdev.flatlaf.intellijthemes.FlatNordIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("One Dark",com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Dark",com.formdev.flatlaf.intellijthemes.FlatSolarizedDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Light",com.formdev.flatlaf.intellijthemes.FlatSolarizedLightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Spacegray",com.formdev.flatlaf.intellijthemes.FlatSpacegrayIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Vuesion",com.formdev.flatlaf.intellijthemes.FlatVuesionIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc Dark (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Arc Dark Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatArcDarkContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Atom One Dark (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Atom One Dark Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneDarkContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Atom One Light (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Atom One Light Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatAtomOneLightContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Dracula (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Dracula Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatDraculaContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("GitHub (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("GitHub Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("GitHub Dark (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("GitHub Dark Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Light Owl (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Light Owl Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatLightOwlContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Darker (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Darker Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDarkerContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Deep Ocean (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Deep Ocean Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Lighter (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Lighter Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialLighterContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Oceanic (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Oceanic Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialOceanicContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Palenight (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Material Palenight Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialPalenightContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Monokai Pro (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Monokai Pro Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMonokaiProContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Moonlight (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Moonlight Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMoonlightContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Night Owl (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Night Owl Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatNightOwlContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Dark (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Dark Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedDarkContrastIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Light (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightIJTheme.class.getCanonicalName()));
        lafs.addItem(new LafInfo("Solarized Light Contrast (Material)",com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatSolarizedLightContrastIJTheme.class.getCanonicalName()));
        */
    }

}
