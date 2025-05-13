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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Cambiar a la librería com.auth0.jwt
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TurnControllerTest {

    @Mock
    private TurnService turnService;

    @InjectMocks
    private TurnController turnController;

    private String validToken;
    private String tokenSinClaims;
    private String validToken1;
    private final String SECRET_KEY = "ContraseñaSuperSecreta123";

    @BeforeEach
    void setUp() {
        // Generamos tokens válidos para pruebas
        MockitoAnnotations.openMocks(this);

        Algorithm algorithm = Algorithm.HMAC256("ContraseñaSuperSecreta123");

        validToken = "Bearer " + JWT.create()
                .withClaim("id", "1")
                .withClaim("userName", "usuario")
                .withClaim("email", "email@example.com")
                .withClaim("name", "Nombre")
                .withClaim("role", "Medical_Secretary")
                .withClaim("specialty", "")
                .sign(algorithm);

        validToken1 = "Bearer " + JWT.create()
                .withClaim("id", "1")
                .withClaim("userName", "usuario")
                .withClaim("email", "email@example.com")
                .withClaim("name", "Nombre")
                .withClaim("role", "Dentistry")
                .withClaim("specialty", "")
                .sign(algorithm);

        tokenSinClaims = "Bearer " + JWT.create()
                .sign(algorithm);
    }

    private String generateValidToken(String id, String name, String email, String role, String specialty) {
        long now = System.currentTimeMillis();
        Date expiryDate = new Date(now + 3600000); // Token válido por 1 hora

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

        return "Bearer " + JWT.create()
                .withClaim("id", id)
                .withClaim("userName", id)
                .withClaim("email", email)
                .withClaim("name", name)
                .withClaim("role", role)
                .withClaim("specialty", specialty)
                .withExpiresAt(expiryDate)
                .sign(algorithm);
    }

    @Test
    void createTurn_validToken_shouldCreateTurn() {
        // Arrange
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setUserName("testUser");
        turnDTO.setIdentityDocument("1234567890");
        turnDTO.setRole(UserRol.Estudiante);
        turnDTO.setSpecialization(Specialization.MedicinaGeneral);
        turnDTO.setDisabilitie(Disabilitie.NoTiene);

        when(turnService.createTurn(
                turnDTO.getUserName(),
                turnDTO.getIdentityDocument(),
                turnDTO.getRole(),
                turnDTO.getSpecialization(),
                turnDTO.getDisabilitie()
        )).thenReturn("M-1");

        // Act
        ResponseEntity<Map<String, String>> response = turnController.createTurn(turnDTO, validToken);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("M-1", response.getBody().get("code"));
        assertEquals("Turno creado exitosamente", response.getBody().get("message"));

        verify(turnService).createTurn(
                turnDTO.getUserName(),
                turnDTO.getIdentityDocument(),
                turnDTO.getRole(),
                turnDTO.getSpecialization(),
                turnDTO.getDisabilitie()
        );
    }

    @Test
    void createTurn_invalidToken_shouldReturnUnauthorized() {
        // Arrange
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setUserName("testUser");
        turnDTO.setIdentityDocument("1234567890");
        turnDTO.setRole(UserRol.Estudiante);
        turnDTO.setSpecialization(Specialization.MedicinaGeneral);
        turnDTO.setDisabilitie(Disabilitie.NoTiene);

        // Act
        ResponseEntity<Map<String, String>> response = turnController.createTurn(turnDTO, tokenSinClaims);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).createTurn(any(), any(), any(), any(), any());
    }

    @Test
    void passTurn_validAdminToken_shouldPassTurn() {
        // Arrange
        doNothing().when(turnService).PassTurn("MedicinaGeneral");

        // Act
        ResponseEntity<Map<String, String>> response = turnController.passTurn("MedicinaGeneral", validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turno pasado exitosamente", response.getBody().get("message"));

        verify(turnService).PassTurn("MedicinaGeneral");
    }


    @Test
    void disableTurn_validAdminToken_shouldDisableTurn() {
        // Arrange
        doNothing().when(turnService).DisableTurns("MedicinaGeneral");

        // Act
        ResponseEntity<Map<String, String>> response = turnController.disableTurn("MedicinaGeneral", validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Turnos deshabilitados exitosamente", response.getBody().get("message"));

        verify(turnService).DisableTurns("MedicinaGeneral");
    }

    @Test
    void disableTurn_nonAdminToken_shouldReturnUnauthorized() {
        // Act
        ResponseEntity<Map<String, String>> response = turnController.disableTurn("MedicinaGeneral", validToken1);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).DisableTurns(any());
    }

    @Test
    void getTurns_validToken_shouldReturnTurns() {
        // Arrange
        List<Turn> mockTurns = new ArrayList<>();
        Turn turn1 = new Turn();
        turn1.setCode("M-1");
        turn1.setPatient("Patient One");
        turn1.setSpecialization(Specialization.MedicinaGeneral);
        turn1.setStatus("Activo");
        turn1.setIdentityDocument("1234567890");
        turn1.setRole(UserRol.Estudiante);
        turn1.setDisabilitie(Disabilitie.NoTiene);
        mockTurns.add(turn1);

        when(turnService.getNextTurns("MedicinaGeneral")).thenReturn(mockTurns);

        // Act
        ResponseEntity<?> response = turnController.getTurns("MedicinaGeneral", validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);

        @SuppressWarnings("unchecked")
        List<TurnDTO> responseTurns = (List<TurnDTO>) response.getBody();
        assertEquals(1, responseTurns.size());
        assertEquals("M-1", responseTurns.get(0).getCode());

        verify(turnService).getNextTurns("MedicinaGeneral");
    }

    @Test
    void getTurns_invalidToken_shouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = turnController.getTurns("MedicinaGeneral", tokenSinClaims);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).getNextTurns(any());
    }

    @Test
    void generateReport_validAdminToken_shouldGenerateReport() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(java.time.LocalDate.now());
        reportDTO.setFinalDate(java.time.LocalDate.now());
        reportDTO.setUserRole("Estudiante");

        Report mockReport = new Report();
        mockReport.setTotalTurns(10);

        when(turnService.generateReport(
                reportDTO.getInitialDate(),
                reportDTO.getFinalDate(),
                reportDTO.getUserRole()
        )).thenReturn(mockReport);

        // Act
        ResponseEntity<?> response = turnController.generateReport(reportDTO, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Report);
        assertEquals(10, ((Report) response.getBody()).getTotalTurns());

        verify(turnService).generateReport(
                reportDTO.getInitialDate(),
                reportDTO.getFinalDate(),
                reportDTO.getUserRole()
        );
    }

    @Test
    void generateReport_nonAdminToken_shouldReturnUnauthorized() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setInitialDate(java.time.LocalDate.now());
        reportDTO.setFinalDate(java.time.LocalDate.now());
        reportDTO.setUserRole("Estudiante");

        // Act
        ResponseEntity<?> response = turnController.generateReport(reportDTO, validToken1);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).generateReport(any(), any(), any());
    }

    @Test
    void generateReport_missingDates_shouldReturnBadRequest() {
        // Arrange
        ReportDTO reportDTO = new ReportDTO();
        // No setting dates

        // Act
        ResponseEntity<?> response = turnController.generateReport(reportDTO, validToken);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        // Verify turnService was not called
        verify(turnService, never()).generateReport(any(), any(), any());
    }

    @Test
    void getSpecializations_validToken_shouldReturnSpecializations() {
        // Arrange
        Specialization[] specializations = Specialization.values();
        when(turnService.getSpecializations()).thenReturn(specializations);

        // Act
        ResponseEntity<?> response = turnController.getSpecializations(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Specialization[]);
        assertEquals(specializations.length, ((Specialization[]) response.getBody()).length);

        verify(turnService).getSpecializations();
    }

    @Test
    void getSpecializations_invalidToken_shouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = turnController.getSpecializations(tokenSinClaims);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).getSpecializations();
    }

    @Test
    void getDisabilities_validToken_shouldReturnDisabilities() {
        // Arrange
        Disabilitie[] disabilities = Disabilitie.values();
        when(turnService.getDisabilities()).thenReturn(disabilities);

        // Act
        ResponseEntity<?> response = turnController.getDisabilities(validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Disabilitie[]);
        assertEquals(disabilities.length, ((Disabilitie[]) response.getBody()).length);

        verify(turnService).getDisabilities();
    }

    @Test
    void getDisabilities_invalidToken_shouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = turnController.getDisabilities(tokenSinClaims);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).getDisabilities();
    }

    @Test
    void getInfoActualTurn_validToken_shouldReturnCurrentTurn() {
        // Arrange
        TurnDTO mockTurnDTO = new TurnDTO();
        mockTurnDTO.setCode("M-1");
        mockTurnDTO.setUserName("Patient One");
        mockTurnDTO.setSpecialization(Specialization.MedicinaGeneral);
        mockTurnDTO.setState("Activo");

        when(turnService.getTurnActualTurn(Specialization.MedicinaGeneral)).thenReturn(mockTurnDTO);

        // Act
        ResponseEntity<?> response = turnController.getInfoActualTurn(Specialization.MedicinaGeneral, validToken);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof TurnDTO);
        assertEquals("M-1", ((TurnDTO) response.getBody()).getCode());

        verify(turnService).getTurnActualTurn(Specialization.MedicinaGeneral);
    }

    @Test
    void getInfoActualTurn_invalidToken_shouldReturnUnauthorized() {
        // Act
        ResponseEntity<?> response = turnController.getInfoActualTurn(Specialization.MedicinaGeneral, tokenSinClaims);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("No autorizado"));

        // Verify turnService was not called
        verify(turnService, never()).getTurnActualTurn(any());
    }

    @Test
    void getInfoActualTurn_serviceError_shouldReturnInternalServerError() {
        // Arrange
        when(turnService.getTurnActualTurn(Specialization.MedicinaGeneral))
                .thenThrow(new RuntimeException("No hay turno actual"));

        // Act
        ResponseEntity<?> response = turnController.getInfoActualTurn(Specialization.MedicinaGeneral, validToken);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertTrue(responseBody.get("error").contains("Error al obtener turno actual"));

        verify(turnService).getTurnActualTurn(Specialization.MedicinaGeneral);
    }
}