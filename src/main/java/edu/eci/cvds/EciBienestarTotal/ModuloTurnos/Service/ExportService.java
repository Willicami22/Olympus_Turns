package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;

// Imports específicos para Excel (Apache POI)
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Imports específicos para PDF (iText)
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ExportService {

    public byte[] exportReportToExcel(Report report) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Turnos");

            int rowNum = 0;

            // Crear estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);

            // Título del reporte
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE TURNOS");
            titleCell.setCellStyle(titleStyle);

            // Espaciado
            rowNum++;

            // Información general del reporte
            rowNum = addInfoRow(sheet, rowNum, "Fecha de Generación:",
                    report.getActualDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " " + report.getActualTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    headerStyle, dataStyle);

            rowNum = addInfoRow(sheet, rowNum, "Período del Reporte:",
                    report.getInitialDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                            " - " + report.getFinalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    headerStyle, dataStyle);

            if (report.getUserRole() != null) {
                rowNum = addInfoRow(sheet, rowNum, "Filtro por Rol:", report.getUserRole().toString(), headerStyle, dataStyle);
            }

            // Espaciado
            rowNum++;

            // Estadísticas generales
            Row statsHeaderRow = sheet.createRow(rowNum++);
            Cell statsHeaderCell = statsHeaderRow.createCell(0);
            statsHeaderCell.setCellValue("ESTADÍSTICAS GENERALES");
            statsHeaderCell.setCellStyle(headerStyle);

            rowNum = addInfoRow(sheet, rowNum, "Total de Turnos:", String.valueOf(report.getTotalTurns()), headerStyle, dataStyle);
            rowNum = addInfoRow(sheet, rowNum, "Turnos Completados:", String.valueOf(report.getTurnsCompleted()), headerStyle, dataStyle);

            if (report.getAvarageWaitingTime() != null) {
                rowNum = addInfoRow(sheet, rowNum, "Tiempo Promedio de Espera:",
                        report.getAvarageWaitingTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        headerStyle, dataStyle);
            }

            if (report.getAverageTimeAttention() != null) {
                rowNum = addInfoRow(sheet, rowNum, "Tiempo Promedio de Atención:",
                        report.getAverageTimeAttention().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        headerStyle, dataStyle);
            }

            // Espaciado
            rowNum++;

            // Distribución por rol
            if (!report.getTurnPercentageByRole().isEmpty()) {
                Row roleHeaderRow = sheet.createRow(rowNum++);
                Cell roleHeaderCell = roleHeaderRow.createCell(0);
                roleHeaderCell.setCellValue("DISTRIBUCIÓN POR ROL");
                roleHeaderCell.setCellStyle(headerStyle);

                // Headers de la tabla
                Row tableHeaderRow = sheet.createRow(rowNum++);
                String[] headers = {"Rol", "% Total Turnos", "% Turnos Completados"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = tableHeaderRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Datos de la tabla
                for (UserRol rol : UserRol.values()) {
                    Double totalPercentage = report.getTurnPercentageByRole().get(rol);
                    Double completedPercentage = report.getCompletedPercentageByRole().get(rol);

                    if (totalPercentage != null && totalPercentage > 0) {
                        Row dataRow = sheet.createRow(rowNum++);

                        Cell rolCell = dataRow.createCell(0);
                        rolCell.setCellValue(rol.toString());
                        rolCell.setCellStyle(dataStyle);

                        Cell totalCell = dataRow.createCell(1);
                        totalCell.setCellValue(String.format("%.2f%%", totalPercentage));
                        totalCell.setCellStyle(dataStyle);

                        Cell completedCell = dataRow.createCell(2);
                        completedCell.setCellValue(String.format("%.2f%%", completedPercentage != null ? completedPercentage : 0.0));
                        completedCell.setCellStyle(dataStyle);
                    }
                }
            }

            // Espaciado
            rowNum++;

            // Distribución de discapacidades
            if (!report.getDisabilityPercentagesByRole().isEmpty()) {
                Row disabilityHeaderRow = sheet.createRow(rowNum++);
                Cell disabilityHeaderCell = disabilityHeaderRow.createCell(0);
                disabilityHeaderCell.setCellValue("DISTRIBUCIÓN DE DISCAPACIDADES POR ROL");
                disabilityHeaderCell.setCellStyle(headerStyle);

                for (Map.Entry<UserRol, Map<String, Double>> roleEntry : report.getDisabilityPercentagesByRole().entrySet()) {
                    UserRol rol = roleEntry.getKey();
                    Map<String, Double> disabilities = roleEntry.getValue();

                    if (!disabilities.isEmpty()) {
                        // Subheader para el rol
                        Row rolSubHeaderRow = sheet.createRow(rowNum++);
                        Cell rolSubHeaderCell = rolSubHeaderRow.createCell(0);
                        rolSubHeaderCell.setCellValue("Rol: " + rol.toString());
                        rolSubHeaderCell.setCellStyle(headerStyle);

                        // Headers de discapacidades
                        Row disabilityTableHeaderRow = sheet.createRow(rowNum++);
                        String[] disabilityHeaders = {"Tipo de Discapacidad", "Porcentaje"};
                        for (int i = 0; i < disabilityHeaders.length; i++) {
                            Cell cell = disabilityTableHeaderRow.createCell(i);
                            cell.setCellValue(disabilityHeaders[i]);
                            cell.setCellStyle(headerStyle);
                        }

                        // Datos de discapacidades
                        for (Map.Entry<String, Double> disabilityEntry : disabilities.entrySet()) {
                            Row dataRow = sheet.createRow(rowNum++);

                            Cell typeCell = dataRow.createCell(0);
                            typeCell.setCellValue(disabilityEntry.getKey());
                            typeCell.setCellStyle(dataStyle);

                            Cell percentageCell = dataRow.createCell(1);
                            percentageCell.setCellValue(String.format("%.2f%%", disabilityEntry.getValue()));
                            percentageCell.setCellStyle(dataStyle);
                        }

                        rowNum++; // Espaciado entre roles
                    }
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    public byte[] exportReportToPdf(Report report) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Configurar fuentes
            PdfFont titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // Título principal
            Paragraph title = new Paragraph("REPORTE DE TURNOS")
                    .setFont(titleFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Información general
            document.add(new Paragraph("INFORMACIÓN GENERAL")
                    .setFont(headerFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            document.add(new Paragraph("Fecha de Generación: " +
                    report.getActualDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " " + report.getActualTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .setFont(normalFont));

            document.add(new Paragraph("Período del Reporte: " +
                    report.getInitialDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                    " - " + report.getFinalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(normalFont));

            if (report.getUserRole() != null) {
                document.add(new Paragraph("Filtro por Rol: " + report.getUserRole().toString())
                        .setFont(normalFont));
            }

            document.add(new Paragraph("\n"));

            // Estadísticas generales
            document.add(new Paragraph("ESTADÍSTICAS GENERALES")
                    .setFont(headerFont)
                    .setFontSize(14)
                    .setMarginBottom(10));

            Table statsTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            addTableRow(statsTable, "Total de Turnos:", String.valueOf(report.getTotalTurns()), headerFont, normalFont);
            addTableRow(statsTable, "Turnos Completados:", String.valueOf(report.getTurnsCompleted()), headerFont, normalFont);

            if (report.getAvarageWaitingTime() != null) {
                addTableRow(statsTable, "Tiempo Promedio de Espera:",
                        report.getAvarageWaitingTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        headerFont, normalFont);
            }

            if (report.getAverageTimeAttention() != null) {
                addTableRow(statsTable, "Tiempo Promedio de Atención:",
                        report.getAverageTimeAttention().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        headerFont, normalFont);
            }

            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // Distribución por rol
            if (!report.getTurnPercentageByRole().isEmpty()) {
                document.add(new Paragraph("DISTRIBUCIÓN POR ROL")
                        .setFont(headerFont)
                        .setFontSize(14)
                        .setMarginBottom(10));

                Table roleTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                        .setWidth(UnitValue.createPercentValue(100));

                // Headers
                roleTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Rol").setFont(headerFont)));
                roleTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("% Total Turnos").setFont(headerFont)));
                roleTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("% Turnos Completados").setFont(headerFont)));

                // Datos
                for (UserRol rol : UserRol.values()) {
                    Double totalPercentage = report.getTurnPercentageByRole().get(rol);
                    Double completedPercentage = report.getCompletedPercentageByRole().get(rol);

                    if (totalPercentage != null && totalPercentage > 0) {
                        roleTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(rol.toString()).setFont(normalFont)));
                        roleTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.format("%.2f%%", totalPercentage)).setFont(normalFont)));
                        roleTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.format("%.2f%%", completedPercentage != null ? completedPercentage : 0.0)).setFont(normalFont)));
                    }
                }

                document.add(roleTable);
                document.add(new Paragraph("\n"));
            }

            // Distribución de discapacidades
            if (!report.getDisabilityPercentagesByRole().isEmpty()) {
                document.add(new Paragraph("DISTRIBUCIÓN DE DISCAPACIDADES POR ROL")
                        .setFont(headerFont)
                        .setFontSize(14)
                        .setMarginBottom(10));

                for (Map.Entry<UserRol, Map<String, Double>> roleEntry : report.getDisabilityPercentagesByRole().entrySet()) {
                    UserRol rol = roleEntry.getKey();
                    Map<String, Double> disabilities = roleEntry.getValue();

                    if (!disabilities.isEmpty()) {
                        document.add(new Paragraph("Rol: " + rol.toString())
                                .setFont(headerFont)
                                .setFontSize(12)
                                .setMarginTop(10));

                        Table disabilityTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                                .setWidth(UnitValue.createPercentValue(100));

                        // Headers
                        disabilityTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Tipo de Discapacidad").setFont(headerFont)));
                        disabilityTable.addHeaderCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Porcentaje").setFont(headerFont)));

                        // Datos
                        for (Map.Entry<String, Double> disabilityEntry : disabilities.entrySet()) {
                            disabilityTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(disabilityEntry.getKey()).setFont(normalFont)));
                            disabilityTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.format("%.2f%%", disabilityEntry.getValue())).setFont(normalFont)));
                        }

                        document.add(disabilityTable);
                        document.add(new Paragraph("\n"));
                    }
                }
            }

            document.close();
            return outputStream.toByteArray();
        }
    }

    // Métodos auxiliares para Excel
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private int addInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);

        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);

        return rowNum + 1;
    }

    // Métodos auxiliares para PDF
    private void addTableRow(Table table, String label, String value, PdfFont labelFont, PdfFont valueFont) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label).setFont(labelFont)));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value).setFont(valueFont)));
    }
}