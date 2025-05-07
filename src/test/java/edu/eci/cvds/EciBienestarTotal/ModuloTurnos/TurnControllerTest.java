package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller.TurnController;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.ReportDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class TurnControllerTest {

    @Mock
    private TurnService turnService;

    @InjectMocks
    private TurnController turnController;

    private TurnDTO turnDTO;
    private Turn turn;
    private List<Turn> turnList;

    @BeforeEach
    void setUp() {
        turnDTO = new TurnDTO();
        turnDTO.setUserName("Juan Pérez");
        turnDTO.setIdentityDocument("1234567890");
        turnDTO.setRole("student");
        turnDTO.setPriority(true);
        turnDTO.setSpeciality("Psychology");

        turn = new Turn();
        turn.setCode("P-1");
        turn.setPatient("Juan Pérez");
        turn.setIdentityDocument("1234567890");
        turn.setRole(UserRol.Student);
        turn.setPriority(true);
        turn.setSpecialization(Specialization.Psychology);
        turn.setStatus("Active");
        turn.setDate(LocalDate.now());
        turn.setInitialTime(LocalTime.now());

        turnList = new ArrayList<>();
        turnList.add(turn);

        Turn turn2 = new Turn();
        turn2.setCode("P-2");
        turn2.setPatient("María López");
        turn2.setIdentityDocument("0987654321");
        turn2.setRole(UserRol.Teacher);
        turn2.setPriority(false);
        turn2.setSpecialization(Specialization.Psychology);
        turn2.setStatus("Active");
        turn2.setDate(LocalDate.now());
        turn2.setInitialTime(LocalTime.now().plusMinutes(10));
        turnList.add(turn2);
    }

    @Test
    void testCreateTurn() {
        when(turnService.CreateTurn(anyString(), anyString(), anyString(), anyBoolean(), anyString()))
                .thenReturn("P-1");

        ResponseEntity<Map<String, String>> response = turnController.CreateTurn(turnDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("P-1", response.getBody().get("code"));
        assertEquals("Turno creado exitosamente", response.getBody().get("message"));

        verify(turnService).CreateTurn(
                turnDTO.getUserName(),
                turnDTO.getIdentityDocument(),
                turnDTO.getRole(),
                turnDTO.isPriority(),
                turnDTO.getSpeciality()
        );
    }

    @Test
    void testCreateTurnWithError() {
        // Arrange
        when(turnService.CreateTurn(anyString(), anyString(), anyString(), anyBoolean(), anyString()))
                .thenThrow(new RuntimeException("Error de test"));


        ResponseEntity<Map<String, String>> response = turnController.CreateTurn(turnDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error de test"));
    }

    @Test
    void testPassTurn() {
        // Arrange
        doNothing().when(turnService).PassTurn(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.PassTurn("Psychology");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turno pasado exitosamente", response.getBody().get("message"));

        verify(turnService).PassTurn("Psychology");
    }

    @Test
    void testPassTurnWithError() {
        doThrow(new RuntimeException("Error al pasar turno")).when(turnService).PassTurn(anyString());

        ResponseEntity<Map<String, String>> response = turnController.PassTurn("Psychology");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error al pasar turno"));
    }

    @Test
    void testDisableTurns() {

        doNothing().when(turnService).DisableTurns(anyString());

        ResponseEntity<Map<String, String>> response = turnController.DisableTurn("Psychology");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turnos deshabilitados exitosamente", response.getBody().get("message"));

        verify(turnService).DisableTurns("Psychology");
    }

    @Test
    void testDisableTurnsWithError() {
        // Arrange
        doThrow(new RuntimeException("Error al deshabilitar")).when(turnService).DisableTurns(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.DisableTurn("Psychology");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Error al deshabilitar"));
    }

    @Test
    void testGetTurns() {
        when(turnService.getNextTurns("Psychology")).thenReturn(turnList);

        ResponseEntity<?> response = turnController.getTurns("Psychology");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<?> resultList = (List<?>) response.getBody();
        assertEquals(2, resultList.size());

        assertTrue(resultList.get(0) instanceof TurnDTO);

        TurnDTO firstDTO = (TurnDTO) resultList.get(0);
        assertEquals("P-1", firstDTO.getCode());
        assertEquals("Juan Pérez", firstDTO.getUserName());
        assertEquals("Psychology", firstDTO.getSpeciality());
        assertTrue(firstDTO.isPriority());

        verify(turnService).getNextTurns("Psychology");
    }

    @Test
    void testGetTurnsVoid() {
        // Arrange
        when(turnService.getNextTurns("Psychology")).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<?> response = turnController.getTurns("Psychology");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<?> resultList = (List<?>) response.getBody();
        assertTrue(resultList.isEmpty());
    }

    @Test
    void testGetTurnsWithError() {
        // Arrange
        when(turnService.getNextTurns("Psychology")).thenThrow(new RuntimeException("Error al obtener turnos"));

        // Act
        ResponseEntity<?> response = turnController.getTurns("Psychology");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertTrue(errorMap.get("error").contains("Error al obtener turnos"));
    }

    @Test
    void generateReport_WithValidDates_ReturnsReport() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(LocalDate.of(2025, 1, 1));
        reportDTO.setFinalDate(LocalDate.of(2025, 1, 31));
        reportDTO.setUserRole(UserRol.Student); // Asumiendo que STUDENT es un valor válido de UserRol

        Report mockReport = new Report(); // Asumiendo que existe una clase Report
        when(turnService.generateReport(reportDTO.getInitialDate(), reportDTO.getFinalDate(),
                reportDTO.getUserRole())).thenReturn(mockReport);

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockReport, response.getBody());
        verify(turnService).generateReport(reportDTO.getInitialDate(), reportDTO.getFinalDate(),
                reportDTO.getUserRole());
    }

    @Test
    void generateReport_WithNullInitialDate_ReturnsBadRequest() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(null);
        reportDTO.setFinalDate(LocalDate.of(2025, 1, 31));

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(turnService, never()).generateReport(any(), any(), any());
    }

    @Test
    void generateReport_WithNullFinalDate_ReturnsBadRequest() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(LocalDate.of(2025, 1, 1));
        reportDTO.setFinalDate(null);

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(turnService, never()).generateReport(any(), any(), any());
    }

    @Test
    void generateReport_WithNullUserRole_StillWorksCorrectly() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(LocalDate.of(2025, 1, 1));
        reportDTO.setFinalDate(LocalDate.of(2025, 1, 31));
        reportDTO.setUserRole(null); // UserRole es opcional

        Report mockReport = new Report();
        when(turnService.generateReport(reportDTO.getInitialDate(), reportDTO.getFinalDate(),
                null)).thenReturn(mockReport);

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockReport, response.getBody());
        verify(turnService).generateReport(reportDTO.getInitialDate(), reportDTO.getFinalDate(), null);
    }

    @Test
    void generateReport_WhenServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(LocalDate.of(2025, 1, 1));
        reportDTO.setFinalDate(LocalDate.of(2025, 1, 31));

        when(turnService.generateReport(any(), any(), any()))
                .thenThrow(new RuntimeException("Error generando reporte"));

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
