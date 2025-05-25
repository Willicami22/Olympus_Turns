package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

    @InjectMocks
    private ExportService exportService;

    private Report sampleReport;

    @BeforeEach
    void setUp() {
        sampleReport = new Report();
        sampleReport.setId("test-report-id");
        sampleReport.setActualDate(LocalDate.now());
        sampleReport.setActualTime(LocalTime.now());
        sampleReport.setInitialDate(LocalDate.of(2025, 5, 1));
        sampleReport.setFinalDate(LocalDate.of(2025, 5, 7));
        sampleReport.setUserRole(UserRol.Estudiante);
        sampleReport.setTotalTurns(100);
        sampleReport.setTurnsCompleted(85);
        sampleReport.setAvarageWaitingTime(LocalTime.of(0, 15, 30));
        sampleReport.setAverageTimeAttention(LocalTime.of(0, 20, 45));

        // Agregar porcentajes por rol
        sampleReport.setTurnPercentageByRole(UserRol.Estudiante, 60.0);
        sampleReport.setTurnPercentageByRole(UserRol.Docente, 25.0);
        sampleReport.setTurnPercentageByRole(UserRol.Administrativo, 15.0);

        sampleReport.setCompletedPercentageByRole(UserRol.Estudiante, 50.0);
        sampleReport.setCompletedPercentageByRole(UserRol.Docente, 22.0);
        sampleReport.setCompletedPercentageByRole(UserRol.Administrativo, 13.0);

        // Agregar porcentajes de discapacidades
        sampleReport.addDisabilityPercentage(UserRol.Estudiante, "NoTiene", 80.0);
        sampleReport.addDisabilityPercentage(UserRol.Estudiante, "MayorDeEdad", 15.0);
        sampleReport.addDisabilityPercentage(UserRol.Estudiante, "DisfuncionMotriz", 5.0);

        sampleReport.addDisabilityPercentage(UserRol.Docente, "NoTiene", 70.0);
        sampleReport.addDisabilityPercentage(UserRol.Docente, "MayorDeEdad", 25.0);
        sampleReport.addDisabilityPercentage(UserRol.Docente, "Embarazo", 5.0);
    }

    @Test
    void testExportReportToExcel_ShouldGenerateValidExcelFile() throws IOException {
        // Act
        byte[] excelData = exportService.exportReportToExcel(sampleReport);

        // Assert
        assertNotNull(excelData, "Los datos del Excel no deben ser null");
        assertTrue(excelData.length > 0, "El archivo Excel debe tener contenido");

        // Verificar que tiene el header de archivo Excel (magic bytes)
        // Los archivos XLSX comienzan con los bytes "PK" (0x504B)
        assertEquals(0x50, excelData[0] & 0xFF, "Primer byte debe ser 'P' (0x50)");
        assertEquals(0x4B, excelData[1] & 0xFF, "Segundo byte debe ser 'K' (0x4B)");
    }

    @Test
    void testExportReportToPdf_ShouldGenerateValidPdfFile() throws IOException {
        // Act
        byte[] pdfData = exportService.exportReportToPdf(sampleReport);

        // Assert
        assertNotNull(pdfData, "Los datos del PDF no deben ser null");
        assertTrue(pdfData.length > 0, "El archivo PDF debe tener contenido");

        // Verificar que tiene el header de archivo PDF
        String pdfHeader = new String(pdfData, 0, Math.min(4, pdfData.length));
        assertEquals("%PDF", pdfHeader, "El archivo debe comenzar con '%PDF'");
    }

    @Test
    void testExportReportToExcel_WithMinimalData_ShouldGenerateFile() throws IOException {
        // Arrange
        Report minimalReport = createMinimalReport();

        // Act
        byte[] excelData = exportService.exportReportToExcel(minimalReport);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verificar formato Excel
        assertEquals(0x50, excelData[0] & 0xFF);
        assertEquals(0x4B, excelData[1] & 0xFF);
    }

    @Test
    void testExportReportToPdf_WithMinimalData_ShouldGenerateFile() throws IOException {
        // Arrange
        Report minimalReport = createMinimalReport();

        // Act
        byte[] pdfData = exportService.exportReportToPdf(minimalReport);

        // Assert
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);

        // Verificar formato PDF
        String pdfHeader = new String(pdfData, 0, Math.min(4, pdfData.length));
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void testExportReportToExcel_WithNullTimes_ShouldHandleGracefully() throws IOException {
        // Arrange
        Report reportWithNullTimes = createReportWithNullTimes();

        // Act
        byte[] excelData = exportService.exportReportToExcel(reportWithNullTimes);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verificar que el archivo se generó correctamente a pesar de los valores null
        assertEquals(0x50, excelData[0] & 0xFF);
        assertEquals(0x4B, excelData[1] & 0xFF);
    }

    @Test
    void testExportReportToPdf_WithNullTimes_ShouldHandleGracefully() throws IOException {
        // Arrange
        Report reportWithNullTimes = createReportWithNullTimes();

        // Act
        byte[] pdfData = exportService.exportReportToPdf(reportWithNullTimes);

        // Assert
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);

        // Verificar formato PDF
        String pdfHeader = new String(pdfData, 0, Math.min(4, pdfData.length));
        assertEquals("%PDF", pdfHeader);
    }

    @Test
    void testExportReportToExcel_WithComplexData_ShouldBeValidFormat() throws IOException {
        // Act
        byte[] excelData = exportService.exportReportToExcel(sampleReport);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);

        // Verificar formato válido
        assertEquals(0x50, excelData[0] & 0xFF);
        assertEquals(0x4B, excelData[1] & 0xFF);

        // El archivo con datos complejos debería ser mayor que uno mínimo
        byte[] minimalData = exportService.exportReportToExcel(createMinimalReport());
        assertTrue(excelData.length >= minimalData.length,
                "El archivo con datos complejos debería ser al menos tan grande como uno mínimo");
    }

    @Test
    void testExportReportToPdf_WithComplexData_ShouldBeValidFormat() throws IOException {
        // Act
        byte[] pdfData = exportService.exportReportToPdf(sampleReport);

        // Assert
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);

        // Verificar formato válido
        String pdfHeader = new String(pdfData, 0, Math.min(4, pdfData.length));
        assertEquals("%PDF", pdfHeader);

        // El archivo con datos complejos debería ser mayor que uno mínimo
        byte[] minimalData = exportService.exportReportToPdf(createMinimalReport());
        assertTrue(pdfData.length >= minimalData.length,
                "El archivo con datos complejos debería ser al menos tan grande como uno mínimo");
    }

    @Test
    void testExportReportToExcel_WithEmptyPercentages_ShouldGenerateFile() throws IOException {
        // Arrange
        Report emptyPercentagesReport = createReportWithEmptyPercentages();

        // Act
        byte[] excelData = exportService.exportReportToExcel(emptyPercentagesReport);

        // Assert
        assertNotNull(excelData);
        assertTrue(excelData.length > 0);
        assertEquals(0x50, excelData[0] & 0xFF);
        assertEquals(0x4B, excelData[1] & 0xFF);
    }

    @Test
    void testExportReportToPdf_WithEmptyPercentages_ShouldGenerateFile() throws IOException {
        // Arrange
        Report emptyPercentagesReport = createReportWithEmptyPercentages();

        // Act
        byte[] pdfData = exportService.exportReportToPdf(emptyPercentagesReport);

        // Assert
        assertNotNull(pdfData);
        assertTrue(pdfData.length > 0);
        String pdfHeader = new String(pdfData, 0, Math.min(4, pdfData.length));
        assertEquals("%PDF", pdfHeader);
    }

    // Métodos auxiliares para crear reportes de prueba

    private Report createMinimalReport() {
        Report minimalReport = new Report();
        minimalReport.setId("minimal-report");
        minimalReport.setActualDate(LocalDate.now());
        minimalReport.setActualTime(LocalTime.now());
        minimalReport.setInitialDate(LocalDate.of(2025, 5, 1));
        minimalReport.setFinalDate(LocalDate.of(2025, 5, 1));
        minimalReport.setTotalTurns(0);
        minimalReport.setTurnsCompleted(0);
        return minimalReport;
    }

    private Report createReportWithNullTimes() {
        Report reportWithNullTimes = new Report();
        reportWithNullTimes.setId("null-times-report");
        reportWithNullTimes.setActualDate(LocalDate.now());
        reportWithNullTimes.setActualTime(LocalTime.now());
        reportWithNullTimes.setInitialDate(LocalDate.of(2025, 5, 1));
        reportWithNullTimes.setFinalDate(LocalDate.of(2025, 5, 7));
        reportWithNullTimes.setTotalTurns(50);
        reportWithNullTimes.setTurnsCompleted(40);
        // No se establecen tiempos promedio (quedan null)
        return reportWithNullTimes;
    }

    private Report createReportWithEmptyPercentages() {
        Report emptyPercentagesReport = new Report();
        emptyPercentagesReport.setId("empty-percentages-report");
        emptyPercentagesReport.setActualDate(LocalDate.now());
        emptyPercentagesReport.setActualTime(LocalTime.now());
        emptyPercentagesReport.setInitialDate(LocalDate.of(2025, 5, 1));
        emptyPercentagesReport.setFinalDate(LocalDate.of(2025, 5, 7));
        emptyPercentagesReport.setTotalTurns(10);
        emptyPercentagesReport.setTurnsCompleted(8);
        // No hay porcentajes por rol ni discapacidades
        return emptyPercentagesReport;
    }
}
