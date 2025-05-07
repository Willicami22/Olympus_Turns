package edu.eci.cvds.EciBienestarTotal.ModuloTurnos;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service.TurnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TurnServiceTest {

    @Mock
    private TurnRepository turnRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private TurnService turnService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTurn_ForStudentWithPsychology() {

        String name = "Juan Pérez";
        String identityDocument = "1234567890";
        String role = "student";
        Boolean priority = false;
        String specialization = "Psychology";

        when(turnRepository.countBySpecializationAndDate(Specialization.Psychology, LocalDate.now())).thenReturn(5L);

        String code = turnService.CreateTurn(name, identityDocument, role, priority, specialization);

        assertEquals("P-6", code);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals(name, savedTurn.getPatient());
        assertEquals(identityDocument, savedTurn.getIdentityDocument());
        assertEquals(UserRol.Student, savedTurn.getRole());
        assertEquals(false, savedTurn.getPriority());
        assertEquals(Specialization.Psychology, savedTurn.getSpecialization());
        assertEquals("Active", savedTurn.getStatus());
        assertEquals(LocalDate.now(), savedTurn.getDate());
        assertNotNull(savedTurn.getInitialTime());
        assertEquals("P-6", savedTurn.getCode());
    }

    @Test
    void testCreateTurn_ForTeacherWithGeneralMedicine() {
        String name = "María López";
        String identityDocument = "0987654321";
        String role = "teacher";
        Boolean priority = true;
        String specialization = "General Medicine";

        when(turnRepository.countBySpecializationAndDate(Specialization.GeneralMedicine, LocalDate.now())).thenReturn(10L);

        String code = turnService.CreateTurn(name, identityDocument, role, priority, specialization);

        assertEquals("M-11", code);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals(UserRol.Teacher, savedTurn.getRole());
        assertEquals(true, savedTurn.getPriority());
        assertEquals(Specialization.GeneralMedicine, savedTurn.getSpecialization());
        assertEquals("M-11", savedTurn.getCode());
    }

    @Test
    void testCreateTurn_ForAdminWithDentistry() {
        String name = "Carlos Gómez";
        String identityDocument = "5678901234";
        String role = "admin";
        Boolean priority = false;
        String specialization = "Dentistry";

        when(turnRepository.countBySpecializationAndDate(Specialization.Dentistry, LocalDate.now())).thenReturn(2L);

        String code = turnService.CreateTurn(name, identityDocument, role, priority, specialization);

        assertEquals("O-3", code);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals(UserRol.Administrative, savedTurn.getRole());
        assertEquals(Specialization.Dentistry, savedTurn.getSpecialization());
        assertEquals("O-3", savedTurn.getCode());
    }

    @Test
    void testCreateTurn_ForGeneralServices() {
        String name = "Ana Ramírez";
        String identityDocument = "1122334455";
        String role = "generalServices";
        Boolean priority = true;
        String specialization = "Psychology";

        when(turnRepository.countBySpecializationAndDate(Specialization.Psychology, LocalDate.now())).thenReturn(0L);

        String code = turnService.CreateTurn(name, identityDocument, role, priority, specialization);

        assertEquals("P-1", code);

        ArgumentCaptor<Turn> turnCaptor = ArgumentCaptor.forClass(Turn.class);
        verify(turnRepository).save(turnCaptor.capture());

        Turn savedTurn = turnCaptor.getValue();
        assertEquals(UserRol.GeneralServices, savedTurn.getRole());
    }

    @Test
    void testDisableTurns_ForPsychology() {
        Turn turn1 = new Turn();
        turn1.setStatus("Active");
        Turn turn2 = new Turn();
        turn2.setStatus("Active");
        List<Turn> activeTurns = List.of(turn1, turn2);

        when(turnRepository.findBySpecializationAndDateAndStatus(
                Specialization.Psychology, LocalDate.now(), "Active"))
                .thenReturn(activeTurns);

        turnService.DisableTurns("Psychology");

        ArgumentCaptor<List<Turn>> turnsCaptor = ArgumentCaptor.forClass(List.class);
        verify(turnRepository).saveAll(turnsCaptor.capture());

        List<Turn> savedTurns = turnsCaptor.getValue();
        assertEquals(2, savedTurns.size());
        savedTurns.forEach(turn -> assertEquals("Disabled", turn.getStatus()));
    }

    @Test
    void testDisableTurns_ForGeneralMedicine() {
        List<Turn> activeTurns = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Turn turn = new Turn();
            turn.setStatus("Active");
            activeTurns.add(turn);
        }

        when(turnRepository.findBySpecializationAndDateAndStatus(
                Specialization.GeneralMedicine, LocalDate.now(), "Active"))
                .thenReturn(activeTurns);

        turnService.DisableTurns("General Medicine");

        ArgumentCaptor<List<Turn>> turnsCaptor = ArgumentCaptor.forClass(List.class);
        verify(turnRepository).saveAll(turnsCaptor.capture());

        List<Turn> savedTurns = turnsCaptor.getValue();
        assertEquals(3, savedTurns.size());
        savedTurns.forEach(turn -> assertEquals("Disabled", turn.getStatus()));
    }

    @Test
    void testDisableTurns_ForDentistry() {
        List<Turn> activeTurns = new ArrayList<>();

        when(turnRepository.findBySpecializationAndDateAndStatus(
                Specialization.Dentistry, LocalDate.now(), "Active"))
                .thenReturn(activeTurns);

        turnService.DisableTurns("Dentistry");

        verify(turnRepository).saveAll(eq(activeTurns));
    }

    @Test
    void testPassTurn_WithPriorityTurn() {
        Turn currentTurn = new Turn();
        currentTurn.setStatus("Attending");

        Turn priorityTurn = new Turn();
        priorityTurn.setStatus("Active");
        priorityTurn.setInitialTime(LocalTime.now());
        priorityTurn.setPriority(true);

        when(turnRepository.findFirstBySpecializationAndStatus(Specialization.Psychology, "Attending"))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Psychology), eq(LocalDate.now()), eq("Active")))
                .thenReturn(Optional.of(priorityTurn));

        turnService.PassTurn("Psychology");

        assertEquals("Passed", currentTurn.getStatus());
        assertNotNull(currentTurn.getFinalTime());

        assertEquals("Attending", priorityTurn.getStatus());
        assertNotNull(priorityTurn.getAttendedTime());

        verify(turnRepository, times(2)).save(any(Turn.class));
        verify(turnRepository, never()).findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                any(), any(), any());
    }

    @Test
    void testPassTurn_WithNormalTurn() {
        Turn currentTurn = new Turn();
        currentTurn.setStatus("Attending");

        Turn normalTurn = new Turn();
        normalTurn.setStatus("Active");
        normalTurn.setInitialTime(LocalTime.now());
        normalTurn.setPriority(false);

        when(turnRepository.findFirstBySpecializationAndStatus(Specialization.GeneralMedicine, "Attending"))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.GeneralMedicine), eq(LocalDate.now()), eq("Active")))
                .thenReturn(Optional.empty());

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                eq(Specialization.GeneralMedicine), eq(LocalDate.now()), eq("Active")))
                .thenReturn(Optional.of(normalTurn));

        turnService.PassTurn("General Medicine");

        assertEquals("Passed", currentTurn.getStatus());
        assertNotNull(currentTurn.getFinalTime());

        assertEquals("Attending", normalTurn.getStatus());
        assertNotNull(normalTurn.getAttendedTime());

        verify(turnRepository, times(2)).save(any(Turn.class));
    }

    @Test
    void testPassTurn_WithNoNextTurn() {
        Turn currentTurn = new Turn();
        currentTurn.setStatus("Attending");

        when(turnRepository.findFirstBySpecializationAndStatus(Specialization.Dentistry, "Attending"))
                .thenReturn(Optional.of(currentTurn));

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Dentistry), eq(LocalDate.now()), eq("Active")))
                .thenReturn(Optional.empty());

        when(turnRepository.findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                eq(Specialization.Dentistry), eq(LocalDate.now()), eq("Active")))
                .thenReturn(Optional.empty());

        turnService.PassTurn("Dentistry");

        assertEquals("Passed", currentTurn.getStatus());
        assertNotNull(currentTurn.getFinalTime());

        verify(turnRepository, times(1)).save(any(Turn.class));
    }

    @Test
    void testGetNextTurns_PrioritariosPrimero() {
        Turn t1 = new Turn();
        t1.setPriority(false);
        t1.setInitialTime(LocalTime.of(8, 30));

        Turn t2 = new Turn();
        t2.setPriority(true);
        t2.setInitialTime(LocalTime.of(9, 0));

        Turn t3 = new Turn();
        t3.setPriority(true);
        t3.setInitialTime(LocalTime.of(8, 45));

        Turn t4 = new Turn();
        t4.setPriority(false);
        t4.setInitialTime(LocalTime.of(9, 15));

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Psychology), any(LocalDate.class), eq("Active")))
                .thenReturn(List.of(t1, t2, t3, t4));

        List<Turn> result = turnService.getNextTurns("Psychology");

        assertEquals(4, result.size());
        assertTrue(result.get(0).getPriority());
        assertEquals(LocalTime.of(8, 45), result.get(0).getInitialTime());
        assertTrue(result.get(1).getPriority());
        assertEquals(LocalTime.of(9, 0), result.get(1).getInitialTime());
        assertFalse(result.get(2).getPriority());
        assertEquals(LocalTime.of(8, 30), result.get(2).getInitialTime());
        assertFalse(result.get(3).getPriority());
        assertEquals(LocalTime.of(9, 15), result.get(3).getInitialTime());
    }

    @Test
    void testGetNextTurns_NoTurnos() {
        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Dentistry), any(LocalDate.class), eq("Active")))
                .thenReturn(List.of());

        List<Turn> result = turnService.getNextTurns("Dentistry");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetNextTurns_SoloNoPrioritarios() {
        Turn t1 = new Turn();
        t1.setPriority(false);
        t1.setInitialTime(LocalTime.of(9, 0));

        Turn t2 = new Turn();
        t2.setPriority(false);
        t2.setInitialTime(LocalTime.of(8, 30));

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.GeneralMedicine), any(LocalDate.class), eq("Active")))
                .thenReturn(List.of(t1, t2));

        List<Turn> result = turnService.getNextTurns("General Medicine");

        assertEquals(2, result.size());
        assertEquals(LocalTime.of(8, 30), result.get(0).getInitialTime());
        assertEquals(LocalTime.of(9, 0), result.get(1).getInitialTime());
    }

    @Test
    void testGetNextTurns_TodosPrioritarios() {
        Turn t1 = new Turn();
        t1.setPriority(true);
        t1.setInitialTime(LocalTime.of(10, 0));

        Turn t2 = new Turn();
        t2.setPriority(true);
        t2.setInitialTime(LocalTime.of(9, 0));

        when(turnRepository.findBySpecializationAndDateAndStatus(
                eq(Specialization.Psychology), any(LocalDate.class), eq("Active")))
                .thenReturn(List.of(t1, t2));

        List<Turn> result = turnService.getNextTurns("Psychology");

        assertEquals(2, result.size());
        assertEquals(LocalTime.of(9, 0), result.get(0).getInitialTime());
        assertEquals(LocalTime.of(10, 0), result.get(1).getInitialTime());
    }

}

