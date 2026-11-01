package com.openbravo.pos.reports;

import java.awt.Color;
import java.awt.Paint;
import net.sf.jasperreports.charts.JRChart;
import net.sf.jasperreports.charts.JRChartCustomizer;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * Customizer for JFreeChart in JasperReports.
 * Centralizes the modern palette of 20 unique colors, avoiding duplicate seriesColor definitions in JRXML.
 */
public class KriolosChartCustomizer implements JRChartCustomizer {

    /**
     * Modern palette of 20 unique colors (no duplicates).
     */
    private static final Paint[] PALETTE = new Paint[] {
        Color.decode("#1ABEF3"), // 0: Cyan Blue
        Color.decode("#B6F90D"), // 1: Lime Green
        Color.decode("#FFCC00"), // 2: Golden Yellow
        Color.decode("#FF6B6B"), // 3: Coral Red
        Color.decode("#4D96FF"), // 4: Royal Blue
        Color.decode("#6BCB77"), // 5: Mint Green
        Color.decode("#F77E21"), // 6: Carrot Orange
        Color.decode("#AC6CFF"), // 7: Lavender Purple
        Color.decode("#FF8AAE"), // 8: Pastel Pink
        Color.decode("#130CB7"), // 9: Dark Blue
        Color.decode("#3EC70B"), // 10: Forest Green
        Color.decode("#9A0680"), // 11: Wine / Magenta
        Color.decode("#FF9F29"), // 12: Amber
        Color.decode("#1A5F7A"), // 13: Petrol Blue
        Color.decode("#57606F"), // 14: Slate Gray
        Color.decode("#FF7F50"), // 15: Coral Salmon
        Color.decode("#20B2AA"), // 16: Sea Green
        Color.decode("#B8860B"), // 17: Dark Goldenrod / Bronze
        Color.decode("#8A2BE2"), // 18: Blue Violet
        Color.decode("#2F4F4F")  // 19: Dark Slate Gray / Graphite
    };

    @Override
    public void customize(JFreeChart chart, JRChart jasperChart) {
        if (chart == null) {
            return;
        }

        Plot plot = chart.getPlot();
        if (plot == null) {
            return;
        }

        // Apply global drawing supplier with custom palette
        plot.setDrawingSupplier(new DefaultDrawingSupplier(
            PALETTE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
            DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));

        // Customize CategoryPlot (bar, column, line, etc.)
        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            CategoryItemRenderer renderer = categoryPlot.getRenderer();
            if (renderer != null) {
                for (int i = 0; i < PALETTE.length; i++) {
                    renderer.setSeriesPaint(i, PALETTE[i]);
                }
            }
        }
        // Customize XYPlot (time series, scatter, etc.)
        else if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot) plot;
            XYItemRenderer renderer = xyPlot.getRenderer();
            if (renderer != null) {
                for (int i = 0; i < PALETTE.length; i++) {
                    renderer.setSeriesPaint(i, PALETTE[i]);
                }
            }
        }
        // Customize PiePlot
        else if (plot instanceof PiePlot) {
            // PiePlot inherits the color sequence from the DefaultDrawingSupplier configured above
        }
    }
}
