/*
 * Copyright (C) 2026 KriolOS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.openbravo.pos.printer.pdf;

import com.openbravo.pos.graphiccode.GraphicCode;
import com.openbravo.pos.spi.hardware.PosImage;
import com.openbravo.pos.spi.hardware.printer.DevicePrinter;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

/**
 * Abstract template for PDF-based printer devices in KriolOS POS. Encapsulates
 * receipt buffering, text-flow logic, and PDFBox rendering.
 *
 * @author KriolOS
 */
public abstract class BasePdfDevicePrinter implements DevicePrinter {

    protected final String printerName;
    protected final File outputDirectory;

    protected final List<ReceiptItem> receiptBuffer = new ArrayList<>();
    private StringBuilder currentLineBuilder = null;

    protected BasePdfDevicePrinter(String printerName, File outputDirectory) {
        this.printerName = printerName;
        this.outputDirectory = outputDirectory;
        if (outputDirectory != null && !outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
    }

    // =========================================================================
    // PROFILE TEMPLATE METHODS (Subclasses implement these)
    // =========================================================================
    public abstract float getPageWidth();

    public abstract float getMarginHorizontal();

    public abstract float getMarginVertical();

    public abstract float getLineHeight();

    public abstract float getFontSize();

    public abstract int getLineLength();

    public abstract boolean isContinuousRoll();

    // =========================================================================
    // DEVICE PRINTER CONTRACT IMPLEMENTATION
    // =========================================================================
    @Override
    public String getPrinterName() {
        return printerName;
    }

    @Override
    public void reset() {
        receiptBuffer.clear();
        currentLineBuilder = null;
    }

    @Override
    public void beginReceipt() {
        reset();
    }

    @Override
    public void beginLine(int textSize) {
        currentLineBuilder = new StringBuilder();
    }

    @Override
    public void printText(int style, String text) {
        if (currentLineBuilder != null && text != null) {
            currentLineBuilder.append(text);
        }
    }

    @Override
    public void endLine() {
        if (currentLineBuilder != null) {
            receiptBuffer.add(new TextItem(currentLineBuilder.toString()));
            currentLineBuilder = null;
        }
    }

    @Override
    public void printImage(PosImage image) {
        byte[] imageData = image.getBytes();
        if (imageData != null && imageData.length > 0) {
            receiptBuffer.add(new ImageItem(imageData));
        }
    }

    @Override
    public void printLogo() {
        // Concrete implementations or settings manager can supply logo data
    }

    @Override
    public void printBarCode(String type, String position, String code) {
        receiptBuffer.add(new BufferedImageItem(GraphicCode.getBarcode(code, type)));
    }

    @Override
    public void printQRCode(String code, int size, char errorCorrection) {
        receiptBuffer.add(new BufferedImageItem(GraphicCode.getCode(code, "QRCODE")));
    }

    @Override
    public void endReceipt() {
        try {
            byte[] pdfBytes = generatePdfDocument();
            if (outputDirectory != null) {
                savePdf(pdfBytes);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error rendering PDF receipt for " + printerName, e);
        }
        finally {
            reset();
        }
    }

    @Override
    public void openDrawer() {
        // Headless PDF devices have no physical drawer
    }

    // =========================================================================
    // PDF GENERATION ENGINE
    // =========================================================================
    public byte[] generatePdfDocument() throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDFont font = new PDType1Font(Standard14Fonts.FontName.COURIER);

            if (isContinuousRoll()) {
                renderContinuousRoll(doc, font);
            } else {
                renderPagedDocument(doc, font);
            }

            // Hook for subclasses (e.g., Factur-X / PDF/A-3b metadata attachment)
            onDocumentCompiled(doc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Renders a single continuous roll ticket (used for 58mm and 80mm thermal
     * receipts).
     */
    private void renderContinuousRoll(PDDocument doc, PDFont font) throws IOException {
        float dynamicHeight = (getMarginVertical() * 2);
        for (ReceiptItem item : receiptBuffer) {
            if (item instanceof TextItem) {
                dynamicHeight += getLineHeight();
            } else if (item instanceof ImageItem) {
                dynamicHeight += 60.0f;
            } else if (item instanceof BufferedImageItem imageItem) {
                dynamicHeight += imageItem.getHeight();
            }
        }
        dynamicHeight = Math.max(dynamicHeight, 100.0f);

        PDRectangle bounds = new PDRectangle(getPageWidth(), dynamicHeight);
        PDPage page = new PDPage(bounds);
        doc.addPage(page);

        float currentY = dynamicHeight - getMarginVertical();
        try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
            for (ReceiptItem item : receiptBuffer) {
                if (item instanceof TextItem textItem) {
                    stream.beginText();
                    stream.setFont(font, getFontSize());
                    stream.newLineAtOffset(getMarginHorizontal(), currentY);
                    stream.showText(sanitizeText(textItem.text));
                    stream.endText();
                    currentY -= getLineHeight();
                } else if (item instanceof ImageItem imgItem) {
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imgItem.data, "img");
                    float usableWidth = getPageWidth() - (2 * getMarginHorizontal());
                    float imgWidth = Math.min(pdImage.getWidth(), usableWidth);
                    float imgHeight = (imgWidth / pdImage.getWidth()) * pdImage.getHeight();
                    currentY -= imgHeight;
                    stream.drawImage(pdImage, getMarginHorizontal(), currentY, imgWidth, imgHeight);
                    currentY -= 6.0f;
                } else if (item instanceof BufferedImageItem imgItem) {
                    PDImageXObject pdImage = LosslessFactory.createFromImage(doc, imgItem.image);
                    float usableWidth = getPageWidth() - (2 * getMarginHorizontal());
                    float imgWidth = Math.min(pdImage.getWidth(), usableWidth);
                    float imgHeight = (imgWidth / pdImage.getWidth()) * pdImage.getHeight();
                    currentY -= imgHeight;
                    stream.drawImage(pdImage, getMarginHorizontal(), currentY, imgWidth, imgHeight);
                    currentY -= imgItem.getHeight();
                }
            }
        }
    }

    /**
     * Renders multiple discrete pages (used for standard A4 invoice layouts).
     */
    private void renderPagedDocument(PDDocument doc, PDFont font) throws IOException {
        PDRectangle pageSize = PDRectangle.A4;
        PDPage page = new PDPage(pageSize);
        doc.addPage(page);

        float currentY = pageSize.getHeight() - getMarginVertical();
        PDPageContentStream stream = new PDPageContentStream(doc, page);

        for (ReceiptItem item : receiptBuffer) {
            // Check page overflow
            if (currentY <= getMarginVertical() + getLineHeight()) {
                stream.close();
                page = new PDPage(pageSize);
                doc.addPage(page);
                currentY = pageSize.getHeight() - getMarginVertical();
                stream = new PDPageContentStream(doc, page);
            }

            if (item instanceof TextItem textItem) {
                stream.beginText();
                stream.setFont(font, getFontSize());
                stream.newLineAtOffset(getMarginHorizontal(), currentY);
                stream.showText(sanitizeText(textItem.text));
                stream.endText();
                currentY -= getLineHeight();
            } else if (item instanceof ImageItem imgItem) {
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, imgItem.data, "img");
                float usableWidth = getPageWidth() - (2 * getMarginHorizontal());
                float imgWidth = Math.min(pdImage.getWidth(), usableWidth);
                float imgHeight = (imgWidth / pdImage.getWidth()) * pdImage.getHeight();
                currentY -= imgHeight;
                stream.drawImage(pdImage, getMarginHorizontal(), currentY, imgWidth, imgHeight);
                currentY -= 6.0f;
            } else if (item instanceof BufferedImageItem imgItem) {
                PDImageXObject pdImage = LosslessFactory.createFromImage(doc, imgItem.image);
                float usableWidth = getPageWidth() - (2 * getMarginHorizontal());
                float imgWidth = Math.min(pdImage.getWidth(), usableWidth);
                float imgHeight = (imgWidth / pdImage.getWidth()) * pdImage.getHeight();
                currentY -= imgHeight;
                stream.drawImage(pdImage, getMarginHorizontal(), currentY, imgWidth, imgHeight);
                currentY -= 6.0f;
            }
        }
        stream.close();
    }

    /**
     * Optional hook for subclasses to inject document catalog metadata or PDF/A
     * dictionaries.
     */
    protected void onDocumentCompiled(PDDocument doc) throws IOException {
        // Default: no-op
    }

    private void savePdf(byte[] pdfBytes) throws IOException {
        String filename = "doc_" + System.currentTimeMillis() + ".pdf";
        File targetFile = new File(outputDirectory, filename);
        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(pdfBytes);
        }
    }

    private String sanitizeText(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\t", "    ");
    }

    // --- Buffer Elements ---
    protected interface ReceiptItem {
    }

    protected static class TextItem implements ReceiptItem {

        final String text;

        TextItem(String text) {
            this.text = text;
        }
    }

    protected static class ImageItem implements ReceiptItem {

        final byte[] data;

        ImageItem(byte[] data) {
            this.data = data;
        }
    }

    protected static class BufferedImageItem implements ReceiptItem {

        final BufferedImage image;

        BufferedImageItem(BufferedImage image) {
            this.image = image;
        }
        
        public int getHeight(){
            return image.getHeight();
        }
    }
}
