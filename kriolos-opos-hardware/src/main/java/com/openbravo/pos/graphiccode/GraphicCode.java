/*
 * Copyright (C) 2026 Kriolos
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
package com.openbravo.pos.graphiccode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.*;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.openbravo.basic.BasicException;
import com.openbravo.format.Formats;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Util class to generate 1D (Baarcode) and 2D(QrCode) 
 * @author poolborges
 */
public class GraphicCode {

    /**
     * Generate Barcode (EAN13) image with default size (width: 200, heigth: 50)
     * @param barcodeText
     * @return Image
     * @throws Exception 
     */
    public static BufferedImage generateEAN13BarcodeImage(String barcodeText) throws Exception {
        return generateEAN13BarcodeImage(barcodeText, 200, 50);
    }

    public static BufferedImage generateEAN13BarcodeImage(String barcodeText,
            int width,int height) throws Exception {
        EAN13Writer barcodeWriter = new EAN13Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.EAN_13, width, height);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
        return generateQRCodeImage(barcodeText, 150);
    }
    public static BufferedImage generateQRCodeImage(String barcodeText, int sizeWH) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix
                = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, sizeWH, sizeWH);

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public static BufferedImage getQRCode(String textcode) {
        return getQRCode(textcode, 150);
    }
    public static  BufferedImage getQRCode(String textcode, int size) {
        BitMatrix bitMatrix = null;
        try {
            if (textcode.startsWith("zatcaksa:")) {
                String[] tlvs = textcode.substring(9).split("#");
                if (tlvs.length > 0) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    for (int j = 0; j < tlvs.length; j++) {
                        if (j == 2) {
                            tlvs[j] = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).format(Formats.TIMESTAMP.parseValue(tlvs[j]));
                        }
                        baos.write((byte) j + 1);
                        byte[] dataBytes = tlvs[j].getBytes("UTF-8");
                        baos.write((byte) dataBytes.length);
                        baos.write(dataBytes);
                    }
                    textcode = Base64.getEncoder().encodeToString(baos.toByteArray());
                }
            }
            HashMap<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            bitMatrix = qrCodeWriter.encode(textcode, BarcodeFormat.QR_CODE, size, size, hintMap);
            int imageWidth = bitMatrix.getWidth();
            BufferedImage image = new BufferedImage(imageWidth, imageWidth, 1);
            image.createGraphics();
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, imageWidth, imageWidth);
            graphics.setColor(Color.BLACK);
            for (int i = 0; i < imageWidth; i++) {
                for (int j = 0; j < imageWidth; j++) {
                    if (bitMatrix.get(i, j)) {
                        graphics.fillRect(i, j, 1, 1);
                    }
                }
            }
        } catch (WriterException | BasicException | IOException e) {
            return null;
        }
        
        return  bitMatrix != null ? MatrixToImageWriter.toBufferedImage(bitMatrix) : null;
    }

    public static BufferedImage getCode(String textcode, String codeType) {
        return getBarcode(textcode, codeType, 0, 0);
    }

    public static  BufferedImage getBarcode(String textcode, String codeType) {
        return getBarcode(textcode, codeType, 0, 0);
    }

    public static  BufferedImage getBarcode(String codeText, String codeType, int bcWidth, int bcHeight) {
        bcWidth = (codeText.length() >= 8) ? 200 : bcWidth;
        bcWidth = (bcWidth == 0) ? 150 : bcWidth;
        bcHeight = (bcHeight == 0) ? 20 : bcHeight;
        BitMatrix bitMatrix = null;
        try {
            Writer writer;
            HashMap<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            switch (codeType) {
                case "QR_CODE":
                case "QRCODE":
                case "QR-CODE":
                    return getQRCode(codeText, bcWidth);
                case "CODE_39":
                case "CODE39":
                case "CODE-39":
                    writer = new Code39Writer();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.CODE_39, bcWidth, bcHeight, hintMap);
                    break;
                case "CODE_93":
                case "CODE93":
                case "CODE-93":
                    writer = new Code93Writer();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.CODE_93, bcWidth, bcHeight, hintMap);
                    break;
                case "CODE_128":
                case "CODE128":
                case "CODE-128":
                    writer = new Code128Writer();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.CODE_128, bcWidth, bcHeight, hintMap);
                    break;
                case "EAN_13":
                case "EAN13":
                case "EAN-13":
                    writer = new EAN13Writer();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.EAN_13, bcWidth, bcHeight, hintMap);
                    break;
                case "EAN_8":
                case "EAN8":
                case "EAN-8":
                    writer = new EAN8Writer();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.EAN_8, bcWidth, bcHeight, hintMap);
                    break;
                case "CODABAR":
                    writer = new CodaBarWriter();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.CODABAR, bcWidth, bcHeight, hintMap);
                    break;
                case "UPC_A":
                case "UPCA":
                case "UPC-A":
                    writer = new UPCAWriter();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.UPC_A, bcWidth, bcHeight, hintMap);
                    break;
                case "UPC_E":
                case "UPCE":
                case "UPC-E":
                    writer = new UPCEWriter();
                    bitMatrix = writer.encode(codeText, BarcodeFormat.UPC_E, bcWidth, bcHeight, hintMap);
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(GraphicCode.class.getName()).log(Level.SEVERE, (String) null, (Throwable) ex);
        }
        
        return  bitMatrix != null ? MatrixToImageWriter.toBufferedImage(bitMatrix) : null;
    }
    
    
    public static BufferedImage generateQRCode(String text, int width, int height, CodePosition position) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        BufferedImage qrImg = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return addHumanReadableText(qrImg, text, position);
    }

    public static BufferedImage generateBarcode(String text, int width, int height, CodePosition position) throws Exception {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(text, BarcodeFormat.CODE_128, width, height);
        BufferedImage barcodeImg = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return addHumanReadableText(barcodeImg, text, position);
    }
    
    // Adiciona o texto legível por humanos ao redor de qualquer BufferedImage (QR ou Barcode)
    private static BufferedImage addHumanReadableText(BufferedImage codeImg, String text, CodePosition position) {
        if (position == CodePosition.NONE || text == null || text.trim().isEmpty()) {
            return codeImg;
        }

        int width = codeImg.getWidth();
        int height = codeImg.getHeight();
        int textSpace = 20; // Espaço vertical reservado para o texto

        // Criar uma nova tela estendida verticalmente
        BufferedImage combinedImg = new BufferedImage(width, height + textSpace, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combinedImg.createGraphics();

        // Preencher o fundo de branco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height + textSpace);

        // Configurar a fonte
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics metrics = g.getFontMetrics();
        int textX = (width - metrics.stringWidth(text)) / 2; // Centralizar o texto horizontalmente
        int textY;
        int codeY;

        if (position == CodePosition.TOP) {
            // Texto em cima, código em baixo
            textY = metrics.getAscent() + 2;
            codeY = textSpace;
        } else {
            // Texto em baixo, código em cima (CodePosition.BOTTOM)
            codeY = 0;
            textY = height + metrics.getAscent() + 2;
        }

        // Desenhar os elementos na nova imagem combinada
        g.drawImage(codeImg, 0, codeY, null);
        g.drawString(text, textX, textY);
        g.dispose();

        return combinedImg;
    }


    private static BufferedImage createBarcode(BitMatrix bitMatrix, String codeText) {
        int imageWidth = bitMatrix.getWidth();
        int imageHeight = bitMatrix.getHeight();
        int fontSize = 12;
        int areaHeigth = imageHeight + fontSize + 2;
        BufferedImage image = new BufferedImage(imageWidth, areaHeigth, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imageWidth, areaHeigth);
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        if (codeText != null) {

            //Set X position 
            int textX = imageWidth / 4;
            // Set Y position
            int textY = areaHeigth - 4;

            //GENERATE HUMAN REDABLE IN BOTTON POSITION
            //Font font = graphics.getFont().deriveFont((float)fontSize);
            Font font = new Font("Monospaced", Font.PLAIN, fontSize);
            graphics.setFont(font);
            graphics.setColor(Color.BLACK);
            graphics.drawString(codeText, textX, textY);
        }

        return image;
    }
    
    public enum CodePosition {
        TOP, BOTTOM, NONE
    }
}
