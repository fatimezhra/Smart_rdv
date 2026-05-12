package com.example.demo.services;

import com.example.demo.entities.RendezVous;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public byte[] generateAppointmentPdf(RendezVous rendezVous) throws DocumentException, IOException, WriterException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // Create PDF document
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // Add title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD);
        Paragraph title = new Paragraph("Appointment Confirmation", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Create appointment details table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(80);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);

        // Add table headers
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

        // Patient Name
        addTableRow(table, "Patient Name:", rendezVous.getUser().getName(), headerFont, cellFont);
        
        // Appointment Date
        String formattedDate = rendezVous.getDate() != null ? rendezVous.getDate().format(DATE_FORMATTER) : "N/A";
        addTableRow(table, "Appointment Date:", formattedDate, headerFont, cellFont);
        
        // Appointment Time
        String formattedTime = rendezVous.getHeure() != null ? rendezVous.getHeure().format(TIME_FORMATTER) : "N/A";
        addTableRow(table, "Appointment Time:", formattedTime, headerFont, cellFont);
        
        // Doctor Name (using a placeholder since there's no doctor field in RendezVous)
        addTableRow(table, "Doctor:", "Dr. Medical Center", headerFont, cellFont);
        
        // Status
        String status = rendezVous.getStatut() != null ? rendezVous.getStatut().toString() : "N/A";
        addTableRow(table, "Status:", status, headerFont, cellFont);

        document.add(table);

        // Generate QR Code
        String qrContent = generateQRContent(rendezVous);
        Image qrCode = generateQRCode(qrContent, 150, 150);
        qrCode.setAlignment(Element.ALIGN_CENTER);
        qrCode.setSpacingBefore(20);
        qrCode.setSpacingAfter(20);
        document.add(qrCode);

        // Add QR Code label
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
        Paragraph qrLabel = new Paragraph("Scan QR code for appointment details", labelFont);
        qrLabel.setAlignment(Element.ALIGN_CENTER);
        qrLabel.setSpacingAfter(20);
        document.add(qrLabel);

        // Add footer
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
        Paragraph footer = new Paragraph("Please arrive 15 minutes before your appointment time.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    private void addTableRow(PdfPTable table, String header, String value, Font headerFont, Font cellFont) {
        PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.setPadding(8);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, cellFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(8);
        
        table.addCell(headerCell);
        table.addCell(valueCell);
    }

    private String generateQRContent(RendezVous rendezVous) {
        StringBuilder content = new StringBuilder();
        content.append("Appointment Details:\n");
        content.append("Patient: ").append(rendezVous.getUser().getName()).append("\n");
        content.append("Date: ").append(rendezVous.getDate() != null ? rendezVous.getDate().format(DATE_FORMATTER) : "N/A").append("\n");
        content.append("Time: ").append(rendezVous.getHeure() != null ? rendezVous.getHeure().format(TIME_FORMATTER) : "N/A").append("\n");
        content.append("Doctor: Dr. Medical Center\n");
        content.append("Status: ").append(rendezVous.getStatut() != null ? rendezVous.getStatut().toString() : "N/A");
        return content.toString();
    }

    private Image generateQRCode(String text, int width, int height) throws WriterException, BadElementException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        
        byte[] pngData = pngOutputStream.toByteArray();
        return Image.getInstance(pngData);
    }
}
