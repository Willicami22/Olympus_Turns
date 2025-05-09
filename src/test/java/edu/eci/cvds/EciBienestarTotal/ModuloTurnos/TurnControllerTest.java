package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Controller.TurnController;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.ReportDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TurnControllerTest {

    @Mock
    private TurnService turnService;

    @InjectMocks
    private TurnController turnController;

    private TurnDTO sampleTurnDTO;
    private Turn sampleTurn;
    private Report sampleReport;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup sample TurnDTO
        sampleTurnDTO = new TurnDTO();
        sampleTurnDTO.setUserName("Juan Pérez");
        sampleTurnDTO.setIdentityDocument("1023456789");
        sampleTurnDTO.setRole(UserRol.Estudiante);
        sampleTurnDTO.setSpecialization(Specialization.MedicinaGeneral);
        sampleTurnDTO.setDisabilitie(Disabilitie.NoTiene);
        sampleTurnDTO.setCode("M-1");
        sampleTurnDTO.setState("Activo");

        // Setup sample Turn
        sampleTurn = new Turn();
        sampleTurn.setPatient("Juan Pérez");
        sampleTurn.setIdentityDocument("1023456789");
        sampleTurn.setRole(UserRol.Estudiante);
        sampleTurn.setSpecialization(Specialization.MedicinaGeneral);
        sampleTurn.setDisabilitie(Disabilitie.NoTiene);
        sampleTurn.setCode("M-1");
        sampleTurn.setStatus("Activo");
        sampleTurn.setDate(LocalDate.now());
        sampleTurn.setInitialTime(LocalTime.now());
        sampleTurn.setPriority(false);

        // Setup sample Report
        sampleReport = new Report();
        sampleReport.setActualDate(LocalDate.now());
        sampleReport.setActualTime(LocalTime.now());
        sampleReport.setInitialDate(LocalDate.now().minusDays(7));
        sampleReport.setFinalDate(LocalDate.now());
        sampleReport.setTotalTurns(10);
        sampleReport.setTurnsCompleted(8);
        sampleReport.setAvarageWaitingTime(LocalTime.of(0, 15, 0));
        sampleReport.setAverageTimeAttention(LocalTime.of(0, 25, 0));
    }

    @Test
    void createTurn_Success() {
        // Arrange
        when(turnService.createTurn(
                anyString(),
                anyString(),
                any(UserRol.class),
                any(Specialization.class),
                any(Disabilitie.class)
        )).thenReturn("M-1");

        // Act
        ResponseEntity<Map<String, String>> response = turnController.createTurn(sampleTurnDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("M-1", response.getBody().get("code"));
        assertEquals("Turno creado exitosamente", response.getBody().get("message"));

        verify(turnService).createTurn(
                eq(sampleTurnDTO.getUserName()),
                eq(sampleTurnDTO.getIdentityDocument()),
                eq(sampleTurnDTO.getRole()),
                eq(sampleTurnDTO.getSpecialization()),
                eq(sampleTurnDTO.getDisabilitie())
        );
    }

    @Test
    void createTurn_ThrowsException() {
        // Arrange
        when(turnService.createTurn(
                anyString(),
                anyString(),
                any(UserRol.class),
                any(Specialization.class),
                any(Disabilitie.class)
        )).thenThrow(new RuntimeException("Error de prueba"));

        // Act
        ResponseEntity<Map<String, String>> response = turnController.createTurn(sampleTurnDTO);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al crear el turno: Error de prueba", response.getBody().get("error"));
    }

    @Test
    void passTurn_Success() {
        // Arrange
        String specialization = "MedicinaGeneral";
        doNothing().when(turnService).PassTurn(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.passTurn(specialization);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turno pasado exitosamente", response.getBody().get("message"));

        verify(turnService).PassTurn(specialization);
    }

    @Test
    void passTurn_NoTurnsAvailable() {
        // Arrange
        String specialization = "MedicinaGeneral";
        doThrow(new NoSuchElementException("No hay turnos disponibles para la especialidad: MedicinaGeneral"))
                .when(turnService).PassTurn(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.passTurn(specialization);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al pasar el turno: No hay turnos disponibles para la especialidad: MedicinaGeneral",
                response.getBody().get("error"));
    }

    @Test
    void disableTurn_Success() {
        // Arrange
        String specialization = "MedicinaGeneral";
        doNothing().when(turnService).DisableTurns(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.disableTurn(specialization);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turnos deshabilitados exitosamente", response.getBody().get("message"));

        verify(turnService).DisableTurns(specialization);
    }

    @Test
    void disableTurn_ThrowsException() {
        // Arrange
        String specialization = "MedicinaGeneral";
        doThrow(new RuntimeException("Error al deshabilitar"))
                .when(turnService).DisableTurns(anyString());

        // Act
        ResponseEntity<Map<String, String>> response = turnController.disableTurn(specialization);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error al deshabilitar los turnos: Error al deshabilitar",
                response.getBody().get("error"));
    }

    @Test
    void getTurns_Success() {
        // Arrange
        String specialization = "MedicinaGeneral";
        List<Turn> turnList = new ArrayList<>();
        turnList.add(sampleTurn);

        when(turnService.getNextTurns(anyString())).thenReturn(turnList);

        // Act
        ResponseEntity<?> response = turnController.getTurns(specialization);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<TurnDTO> turnDTOList = (List<TurnDTO>) response.getBody();
        assertEquals(1, turnDTOList.size());
        assertEquals(sampleTurn.getCode(), turnDTOList.get(0).getCode());
        assertEquals(sampleTurn.getPatient(), turnDTOList.get(0).getUserName());

        verify(turnService).getNextTurns(specialization);
    }

    @Test
    void getTurns_ThrowsException() {
        // Arrange
        String specialization = "MedicinaGeneral";
        when(turnService.getNextTurns(anyString())).thenThrow(new RuntimeException("Error al obtener turnos"));

        // Act
        ResponseEntity<?> response = turnController.getTurns(specialization);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> errorResponse = (Map<String, String>) response.getBody();
        assertEquals("Error al obtener la lista de turnos: Error al obtener turnos",
                errorResponse.get("error"));
    }

    @Test
    void generateReport_Success() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(LocalDate.now().minusDays(7));
        reportDTO.setFinalDate(LocalDate.now());
        reportDTO.setUserRole("ESTUDIANTE");

        when(turnService.generateReport(any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(sampleReport);

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(sampleReport, response.getBody());

        verify(turnService).generateReport(
                eq(reportDTO.getInitialDate()),
                eq(reportDTO.getFinalDate()),
                eq(reportDTO.getUserRole())
        );
    }

    @Test
    void generateReport_MissingDates() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        // Missing initialDate and finalDate

        // Act
        ResponseEntity<Report> response = turnController.generateReport(reportDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getSpecializations_Success() {
        // Arrange
        Specialization[] specializations = Specialization.values();
        when(turnService.getSpecializations()).thenReturn(specializations);

        // Act
        ResponseEntity<Specialization[]> response = turnController.getSpecializations();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(specializations, response.getBody());

        verify(turnService).getSpecializations();
    }

    @Test
    void getDisabilities_Success() {
        // Arrange
        Disabilitie[] disabilities = Disabilitie.values();
        when(turnService.getDisabilities()).thenReturn(disabilities);

        ResponseEntity<Disabilitie[]> response = turnController.getDisabilities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(disabilities, response.getBody());

        verify(turnService).getDisabilities();
    }

    @Test
    void getInfoActualTurn_Success() {
        Specialization specialization = Specialization.Psicologia;
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setCode("P-1");
        turnDTO.setUserName("Ana Gómez");

        when(turnService.getTurnActualTurn(any(Specialization.class))).thenReturn(turnDTO);

        ResponseEntity<TurnDTO> response = turnController.getInfoActualTurn(specialization);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(turnDTO.getCode(), response.getBody().getCode());
        assertEquals(turnDTO.getUserName(), response.getBody().getUserName());

        verify(turnService).getTurnActualTurn(specialization);
    }

    @Test
    void getInfoActualTurn_NoActiveTurn() {
        Specialization specialization = Specialization.Psicologia;
        when(turnService.getTurnActualTurn(any(Specialization.class))).thenReturn(null);

        ResponseEntity<TurnDTO> response = turnController.getInfoActualTurn(specialization);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(turnService).getTurnActualTurn(specialization);
    }
}