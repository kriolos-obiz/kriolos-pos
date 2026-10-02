//    KriolOS POS
//    Copyright (c) 2019-2023 KriolOS
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.reports;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.BaseSentence;
import com.openbravo.pos.util.ReportUtils;
import java.awt.Graphics2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import javax.print.PrintService;
import javax.swing.JPanel;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;

/**
 *
 * @author dev
 */
public class PrintReportUtils {

    private final static System.Logger LOGGER = System.getLogger(PrintReportUtils.class.getName());

    /**
     *
     * @param printerName
     * @param resourcefile
     * @param reportParams
     * @param reportFields
     */
    public static void printReport(String printerName, String resourcefile,
            Map<String, Object> reportParams, Map<String, Object> reportFields) throws BasicException  {

        try {

            JasperReport jr = createJasperReport(resourcefile);

            JasperPrint jp = JasperFillManager.fillReport(jr, reportParams,
                    new JRMapArrayDataSource(new Object[]{reportFields}));

            PrintService service = ReportUtils.getPrintService(printerName);

            JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);

        } catch (Exception ex) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception on print report fromfile: " + resourcefile, ex);
            throw new BasicException("Exception on print report fromfile: " + resourcefile, ex);
        }

    }

    public static JasperReport createJasperReport(String reportFilename) throws BasicException {

        JasperReport jasperReport = null;

        //Try to load report compiled file (.ser)
        if (reportFilename != null) {
            String fullName = reportFilename + ".ser";

            try (InputStream in = PrintReportUtils.class.getResourceAsStream(fullName)) {
                if (in != null) {
                    try (ObjectInputStream oin = new ObjectInputStream(in)) {
                        jasperReport = (JasperReport) oin.readObject();
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception load compiled report file: " + fullName, ex);
                throw new BasicException("Exception load compiled report file: " + fullName, ex);
            }
        }

        //try to load report source file (.j
        if (jasperReport == null && reportFilename != null) {
            String fullName = reportFilename + ".jrxml";
            try (InputStream in = PrintReportUtils.class.getResourceAsStream(fullName)) {
                if (in != null) {
                    //JasperDesign jd = JRXmlLoader.load(in);
                    jasperReport = JasperCompileManager.compileReport(in);
                }
            } catch (IOException | JRException ex) {
                LOGGER.log(System.Logger.Level.WARNING, "Exception load/compile report file: : " + fullName, ex);
                throw new BasicException("Exception load/compile report file: " + fullName, ex);
            }
        }

        if (jasperReport == null) {
            LOGGER.log(System.Logger.Level.WARNING, "Exception create JasperReport for file:" + reportFilename);
            throw new BasicException("Exception create JasperReport for file: " + reportFilename);
        }

        return jasperReport;
    }

    public static void loadReport(JRViewer400 reportviewer, String reportFilename,
            BaseSentence statement, ReportFields fields, Object params,
            Map<String, Object> reportParams)  throws BasicException {

        try {
            JasperReport jasperReport = PrintReportUtils.createJasperReport(reportFilename);
            if (jasperReport != null) {

                JRDataSource data = new JRDataSourceBasic(statement, fields, params);

                JasperPrint jp = JasperFillManager.fillReport(jasperReport, reportParams, data);

                reportviewer.loadJasperPrint(jp);

            }
        } catch (JRException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception fill report: " + reportFilename, ex);
            throw new BasicException("Exception fill report: " + reportFilename, ex);
        }

    }

    public static void loadReport(JRViewer400 reportviewer, String reportFilename, Map<String, Object> reportParams) throws BasicException  {

        try {
            JasperReport jasperReport = JasperCompileManager.compileReport(reportFilename);
            //JasperReport jasperReport = JasperCompileManager.compileReport(getClass().getResourceAsStream("reports" +  "/com/openbravo/reports/voucher" + ".jrxml"));

            if (jasperReport != null) {
                JasperPrint jp = JasperFillManager.fillReport(jasperReport, reportParams, new JREmptyDataSource());

                reportviewer.loadJasperPrint(jp);

                //JasperExportManager.exportReportToPdfFile(jp, "voucher_" + voucherInfo.getVoucherNumber() + ".pdf");
            }
        } catch (JRException ex) {
            LOGGER.log(System.Logger.Level.ERROR, "Exception fill report: " + reportFilename, ex);
            throw new BasicException("Exception fill report: " + reportFilename, ex);
        }
    }

    public static void exportToPdf(JPanel panel, String filePath) throws BasicException  {
        // Ensure the layout is calculated before printing
        if (panel.getWidth() == 0 || panel.getHeight() == 0) {
            panel.setSize(panel.getPreferredSize());
            panel.doLayout();
        }

        // Match the document canvas size with the JPanel size
        Document document = new Document(new com.lowagie.text.Rectangle(panel.getWidth(), panel.getHeight()));

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            PdfWriter writer = PdfWriter.getInstance(document, fos);
            document.open();

            // Access the underlying direct PDF rendering layers
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate template = cb.createTemplate(panel.getWidth(), panel.getHeight());

            // Bind a Java Graphics2D context onto the PDF template canvas
            Graphics2D g2d = template.createGraphics(panel.getWidth(), panel.getHeight());

            // Force the JPanel components to draw themselves into the PDF graphics context
            panel.printAll(g2d);
            g2d.dispose();

            // Render the compiled template layer into the output file coordinates
            cb.addTemplate(template, 0, 0);

        } catch (Exception ex) {
            throw new BasicException("Exception convert JPanel to PDF : " + panel.getName(), ex);
        } finally {
            document.close();
        }
    }
}
