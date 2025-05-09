package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TurnServiceTest {

    @Mock
    private TurnRepository turnRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private TurnService turnService;

    private Turn sampleTurn;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        sampleTurn = new Turn();
        sampleTurn.setId("1");
        sampleTurn.setDate(today);
        sampleTurn.setCode("P-1");
        sampleTurn.setPatient("Test Patient");
        sampleTurn.setIdentityDocument("1234567890");
        sampleTurn.setInitialTime(LocalTime.of(10, 0));
        sampleTurn.setDisabilitie(Disabilitie.NoTiene);
        sampleTurn.setRole(UserRol.Estudiante);
        sampleTurn.setSpecialization(Specialization.Psicologia);
        sampleTurn.setPriority(false);
        sampleTurn.setStatus("Activo");
    }

    @Test
    void testCreateTurn_WithoutDisability() {
        // Arrange
        when(turnRepository.countBySpecializationAndDate(any(Specialization.class), any(LocalDate.class))).thenReturn(0L);
        when(turnRepository.save(any(Turn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = turnService.createTurn("John Doe", "1234567890", UserRol.Estudiante,
                Specialization.Psicologia, Disabilitie.NoTiene);

        // Assert
        assertEquals("P-1", result);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals("John Doe", savedTurn.getPatient());
        assertEquals("1234567890", savedTurn.getIdentityDocument());
        assertEquals(Specialization.Psicologia, savedTurn.getSpecialization());
        assertEquals(UserRol.Estudiante, savedTurn.getRole());
        assertEquals(Disabilitie.NoTiene, savedTurn.getDisabilitie());
        assertEquals("Activo", savedTurn.getStatus());
        assertEquals(today, savedTurn.getDate());
        assertFalse(savedTurn.getPriority());
    }

    @Test
    void testCreateTurn_WithDisability() {
        // Arrange
        when(turnRepository.countBySpecializationAndDate(any(Specialization.class), any(LocalDate.class))).thenReturn(0L);
        when(turnRepository.save(any(Turn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = turnService.createTurn("Jane Doe", "0987654321", UserRol.Administrativo,
                Specialization.MedicinaGeneral, Disabilitie.Embarazo);

        // Assert
        assertEquals("M-1", result);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals("Jane Doe", savedTurn.getPatient());
        assertEquals(Disabilitie.Embarazo, savedTurn.getDisabilitie());
        assertTrue(savedTurn.getPriority());
    }

    @Test
    void testCreateCode_PsicologiaWithExistingTurns() {
        // Arrange
        when(turnRepository.countBySpecializationAndDate(eq(Specialization.Psicologia), any(LocalDate.class))).thenReturn(5L);
        when(turnRepository.save(any(Turn.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = turnService.createTurn("Test User", "1111222233", UserRol.Estudiante,
                Specialization.Psicologia, Disabilitie.NoTiene);

        // Assert
        assertEquals("P-6", result);
    }

    @Test
    void testDisableTurns() {
        // Arrange
        List<Turn> activeTurns = new ArrayList<>();
        Turn turn1 = new Turn();
        turn1.setStatus("Activo");
        Turn turn2 = new Turn();
        turn2.setStatus("Activo");
        activeTurns.add(turn1);
        activeTurns.add(turn2);

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Odontologia), eq(today), eq("Activo")))
                .thenReturn(activeTurns);

        // Act
        turnService.DisableTurns("Odontologia");

        // Assert
        ArgumentCaptor<List<Turn>> turnsCaptor = ArgumentCaptor.forClass(List.class);
        verify(turnRepository).saveAll(turnsCaptor.capture());

        List<Turn> savedTurns = turnsCaptor.getValue();
        assertEquals(2, savedTurns.size());
        assertEquals("Inhabilitado", savedTurns.get(0).getStatus());
        assertEquals("Inhabilitado", savedTurns.get(1).getStatus());
    }

    @Test
    void testPassTurn_WithPriorityTurn() {
        // Arrange
        Turn currentTurn = new Turn();
        currentTurn.setStatus("En Atencion");
        currentTurn.setInitialTime(LocalTime.of(9, 0));

        Turn priorityTurn = new Turn();
        priorityTurn.setStatus("Activo");
        priorityTurn.setPriority(true);
        priorityTurn.setInitialTime(LocalTime.of(9, 30));

        when(turnRepository.findFirstBySpecializationAndStatus(
                eq(Specialization.Psicologia), eq("En Atencion")))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Psicologia), eq(today), eq("Activo")))
                .thenReturn(Optional.of(priorityTurn));

        // Act
        turnService.PassTurn("Psicologia");

        // Assert
        verify(turnRepository, times(2)).save(any(Turn.class));

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository, times(2)).save(turnCaptor.capture());

        List<Turn> savedTurns = turnCaptor.getAllValues();
        assertEquals("Atendido", savedTurns.get(0).getStatus());
        assertNotNull(savedTurns.get(0).getFinalTime());

        assertEquals("En Atencion", savedTurns.get(1).getStatus());
        assertNotNull(savedTurns.get(1).getAttendedTime());
    }

    @Test
    void testPassTurn_WithNoPriorityTurn() {
        // Arrange
        Turn currentTurn = new Turn();
        currentTurn.setStatus("En Atencion");

        Turn normalTurn = new Turn();
        normalTurn.setStatus("Activo");
        normalTurn.setPriority(false);

        when(turnRepository.findFirstBySpecializationAndStatus(
                eq(Specialization.MedicinaGeneral), eq("En Atencion")))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.MedicinaGeneral), eq(today), eq("Activo")))
                .thenReturn(Optional.empty());

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                eq(Specialization.MedicinaGeneral), eq(today), eq("Activo")))
                .thenReturn(Optional.of(normalTurn));

        // Act
        turnService.PassTurn("MedicinaGeneral");

        // Assert
        verify(turnRepository, times(2)).save(any(Turn.class));

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository, times(2)).save(turnCaptor.capture());

        List<Turn> savedTurns = turnCaptor.getAllValues();
        assertEquals("Atendido", savedTurns.get(0).getStatus());
        assertEquals("En Atencion", savedTurns.get(1).getStatus());
    }

    @Test
    void testPassTurn_NoMoreTurns() {
        // Arrange
        Turn currentTurn = new Turn();
        currentTurn.setStatus("En Atencion");

        when(turnRepository.findFirstBySpecializationAndStatus(
                eq(Specialization.Odontologia), eq("En Atencion")))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Odontologia), eq(today), eq("Activo")))
                .thenReturn(Optional.empty());

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Odontologia), eq(today), eq("Activo")))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> turnService.PassTurn("Odontologia"));

        verify(turnRepository, times(1)).save(any(Turn.class));
    }

    @Test
    void testGetNextTurns() {
        // Arrange
        Turn turn1 = new Turn();
        turn1.setPriority(true);
        turn1.setInitialTime(LocalTime.of(9, 30));

        Turn turn2 = new Turn();
        turn2.setPriority(true);
        turn2.setInitialTime(LocalTime.of(10, 0));

        Turn turn3 = new Turn();
        turn3.setPriority(false);
        turn3.setInitialTime(LocalTime.of(9, 0));

        List<Turn> turns = Arrays.asList(turn3, turn1, turn2);

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Psicologia), eq(today), eq("Activo")))
                .thenReturn(turns);

        // Act
        List<Turn> result = turnService.getNextTurns("Psicologia");

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.get(0).getPriority()); // Priority turns first
        assertTrue(result.get(1).getPriority());
        assertFalse(result.get(2).getPriority());

        // Check time ordering within priority group
        assertTrue(result.get(0).getInitialTime().isBefore(result.get(1).getInitialTime()));
    }

    @Test
    void testGenerateReport() {
        // Arrange
        LocalDate from = LocalDate.of(2025, 5, 1);
        LocalDate to = LocalDate.of(2025, 5, 7);

        Turn turn1 = new Turn();
        turn1.setStatus("Atendido");
        turn1.setRole(UserRol.Estudiante);
        turn1.setDisabilitie(Disabilitie.NoTiene);
        turn1.setInitialTime(LocalTime.of(10, 0));
        turn1.setAttendedTime(LocalTime.of(10, 15));
        turn1.setFinalTime(LocalTime.of(10, 30));

        Turn turn2 = new Turn();
        turn2.setStatus("Atendido");
        turn2.setRole(UserRol.Docente);
        turn2.setDisabilitie(Disabilitie.MayorDeEdad);
        turn2.setInitialTime(LocalTime.of(11, 0));
        turn2.setAttendedTime(LocalTime.of(11, 10));
        turn2.setFinalTime(LocalTime.of(11, 25));

        Turn turn3 = new Turn();
        turn3.setStatus("Activo");
        turn3.setRole(UserRol.Estudiante);
        turn3.setDisabilitie(Disabilitie.DisfuncionMotriz);

        List<Turn> allTurns = Arrays.asList(turn1, turn2, turn3);

        when(turnRepository.findByDateBetween(eq(from), eq(to))).thenReturn(allTurns);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report report = turnService.generateReport(from, to, null);

        // Assert
        assertNotNull(report);
        assertEquals(from, report.getInitialDate());
        assertEquals(to, report.getFinalDate());
        assertEquals(3, report.getTotalTurns());
        assertEquals(2, report.getTurnsCompleted());

        // Verify wait time calculation
        assertNotNull(report.getAvarageWaitingTime());

        // Verify attention time calculation
        assertNotNull(report.getAverageTimeAttention());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testGenerateReport_WithRolFilter() {
        // Arrange
        LocalDate from = LocalDate.of(2025, 5, 1);
        LocalDate to = LocalDate.of(2025, 5, 7);

        Turn turn1 = new Turn();
        turn1.setStatus("Atendido");
        turn1.setRole(UserRol.Estudiante);
        turn1.setDisabilitie(Disabilitie.NoTiene);
        turn1.setInitialTime(LocalTime.of(10, 0));
        turn1.setAttendedTime(LocalTime.of(10, 15));
        turn1.setFinalTime(LocalTime.of(10, 30));

        Turn turn2 = new Turn();
        turn2.setStatus("Atendido");
        turn2.setRole(UserRol.Docente);
        turn2.setDisabilitie(Disabilitie.MayorDeEdad);
        turn2.setInitialTime(LocalTime.of(11, 0));
        turn2.setAttendedTime(LocalTime.of(11, 10));
        turn2.setFinalTime(LocalTime.of(11, 25));

        List<Turn> allTurns = Arrays.asList(turn1, turn2);

        when(turnRepository.findByDateBetween(eq(from), eq(to))).thenReturn(allTurns);
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report report = turnService.generateReport(from, to, "Estudiante");

        // Assert
        assertNotNull(report);
        assertEquals(1, report.getTotalTurns());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void testGetTurnSpecialization() {
        // Arrange
        when(turnRepository.findById("1")).thenReturn(Optional.of(sampleTurn));
        when(turnRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Specialization result1 = turnService.getTurnSpecialization("1");
        Specialization result2 = turnService.getTurnSpecialization("nonexistent");

        // Assert
        assertEquals(Specialization.Psicologia, result1);
        assertNull(result2);
    }

    @Test
    void testGetTurnActualTurn() {
        // Arrange
        List<Turn> turns = Collections.singletonList(sampleTurn);
        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Psicologia), eq(today), eq("En Atencion")))
                .thenReturn(turns);

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.MedicinaGeneral), eq(today), eq("En Atencion")))
                .thenReturn(Collections.emptyList());

        // Act
        TurnDTO result1 = turnService.getTurnActualTurn(Specialization.Psicologia);
        TurnDTO result2 = turnService.getTurnActualTurn(Specialization.MedicinaGeneral);

        // Assert
        assertNotNull(result1);
        assertEquals("P-1", result1.getCode());
        assertEquals("Test Patient", result1.getUserName());

        assertNull(result2);
    }

    @Test
    void testToDTO() {
        // Act
        TurnDTO dto = turnService.toDTO(sampleTurn);

        // Assert
        assertNotNull(dto);
        assertEquals("P-1", dto.getCode());
        assertEquals("Test Patient", dto.getUserName());
    }
}