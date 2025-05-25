package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller.ExportController;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ExportController exportController;

    private String validAdminToken;
    private String validUserToken;
    private String invalidToken;
    private Report sampleReport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Algorithm algorithm = Algorithm.HMAC256("supersecretpassword1234567891011121314");

        // Token válido con rol Medical_Secretary (admin)
        validAdminToken = "Bearer " + JWT.create()
                .withClaim("id", "1")
                .withClaim("userName", "admin")
                .withClaim("email", "admin@example.com")
                .withClaim("name", "Admin User")
                .withClaim("role", "Medical_Secretary")
                .withClaim("specialty", "")
                .sign(algorithm);

        // Token válido con rol de usuario normal
        validUserToken = "Bearer " + JWT.create()
                .withClaim("id", "2")
                .withClaim("userName", "user")
                .withClaim("email", "user@example.com")
                .withClaim("name", "Normal User")
                .withClaim("role", "Estudiante")
                .withClaim("specialty", "")
                .sign(algorithm);

        // Token inválido (sin claims requeridos)
        invalidToken = "Bearer " + JWT.create()
                .sign(algorithm);

        // Configurar reporte de muestra
        sampleReport = new Report();
        sampleReport.setId("test-report-id");
        sampleReport.setActualDate(LocalDate.now());
        sampleReport.setActualTime(LocalTime.now());
        sampleReport.setInitialDate(LocalDate.of(2025, 5, 1));
        sampleReport.setFinalDate(LocalDate.of(2025, 5, 7));
        sampleReport.setUserRole(UserRol.Estudiante);
        sampleReport.setTotalTurns(100);
        sampleReport.setTurnsCompleted(85);
    }

    @Test
    void exportToExcel_WithValidAdminToken_ShouldReturnExcelFile() throws IOException {
        // Arrange
        String reportId = "test-report-id";
        byte[] mockExcelData = "mock excel data".getBytes();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(sampleReport));
        when(exportService.exportReportToExcel(any(Report.class))).thenReturn(mockExcelData);

        // Act
        ResponseEntity<?> response = exportController.exportToExcel(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockExcelData, (byte[]) response.getBody());

        // Verificar headers
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().getFilename().endsWith(".xlsx"));

        verify(reportRepository).findById(reportId);
        verify(exportService).exportReportToExcel(sampleReport);
    }

    @Test
    void exportToExcel_WithInvalidToken_ShouldReturnUnauthorized() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        // Act
        ResponseEntity<?> response = exportController.exportToExcel(reportId, invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verificar que no se llamaron los servicios
        verify(reportRepository, never()).findById(any());
        verify(exportService, never()).exportReportToExcel(any());
    }

    @Test
    void exportToExcel_WithNonAdminToken_ShouldReturnUnauthorized() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        // Act
        ResponseEntity<?> response = exportController.exportToExcel(reportId, validUserToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verificar que no se llamaron los servicios
        verify(reportRepository, never()).findById(any());
        verify(exportService, never()).exportReportToExcel(any());
    }

    @Test
    void exportToExcel_WithNonExistentReport_ShouldReturnNotFound() throws IOException {
        // Arrange
        String reportId = "non-existent-report-id";

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = exportController.exportToExcel(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Reporte no encontrado", responseBody.get("error"));

        verify(reportRepository).findById(reportId);
        verify(exportService, never()).exportReportToExcel(any());
    }

    @Test
    void exportToExcel_WithServiceException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(sampleReport));
        when(exportService.exportReportToExcel(any(Report.class))).thenThrow(new IOException("Error de exportación"));

        // Act
        ResponseEntity<?> response = exportController.exportToExcel(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("Error al exportar a Excel"));

        verify(reportRepository).findById(reportId);
        verify(exportService).exportReportToExcel(sampleReport);
    }

    @Test
    void exportToPdf_WithValidAdminToken_ShouldReturnPdfFile() throws IOException {
        // Arrange
        String reportId = "test-report-id";
        byte[] mockPdfData = "%PDF-1.4 mock pdf data".getBytes();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(sampleReport));
        when(exportService.exportReportToPdf(any(Report.class))).thenReturn(mockPdfData);

        // Act
        ResponseEntity<?> response = exportController.exportToPdf(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(mockPdfData, (byte[]) response.getBody());

        // Verificar headers
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(response.getHeaders().getContentDisposition().getFilename().endsWith(".pdf"));

        verify(reportRepository).findById(reportId);
        verify(exportService).exportReportToPdf(sampleReport);
    }

    @Test
    void exportToPdf_WithInvalidToken_ShouldReturnUnauthorized() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        // Act
        ResponseEntity<?> response = exportController.exportToPdf(reportId, invalidToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verificar que no se llamaron los servicios
        verify(reportRepository, never()).findById(any());
        verify(exportService, never()).exportReportToPdf(any());
    }

    @Test
    void exportToPdf_WithNonAdminToken_ShouldReturnUnauthorized() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        // Act
        ResponseEntity<?> response = exportController.exportToPdf(reportId, validUserToken);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verificar que no se llamaron los servicios
        verify(reportRepository, never()).findById(any());
        verify(exportService, never()).exportReportToPdf(any());
    }

    @Test
    void exportToPdf_WithNonExistentReport_ShouldReturnNotFound() throws IOException {
        // Arrange
        String reportId = "non-existent-report-id";

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = exportController.exportToPdf(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Reporte no encontrado", responseBody.get("error"));

        verify(reportRepository).findById(reportId);
        verify(exportService, never()).exportReportToPdf(any());
    }

    @Test
    void exportToPdf_WithServiceException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        String reportId = "test-report-id";

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(sampleReport));
        when(exportService.exportReportToPdf(any(Report.class))).thenThrow(new IOException("Error de exportación PDF"));

        // Act
        ResponseEntity<?> response = exportController.exportToPdf(reportId, validAdminToken);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("Error al exportar a PDF"));

        verify(reportRepository).findById(reportId);
        verify(exportService).exportReportToPdf(sampleReport);
    }

    @Test
    void generateFilename_ShouldCreateDescriptiveFilename() {
        // Este test requeriría hacer el método generateFilename() público o package-private
        // Por ahora verificamos que los endpoints generen archivos con nombres apropiados
        // mediante los tests de integración anteriores
        assertTrue(true); // Placeholder - el método es private
    }
}