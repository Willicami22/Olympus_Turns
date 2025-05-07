package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TurnService {
    @Autowired
    private TurnRepository turnRepository;
    @Autowired
    private ReportRepository reportRepository;

    public String CreateTurn(String name,String IdentityDocument, String role, Boolean priority, String specialization ) {
        Turn turn = new Turn();
        turn.setDate(LocalDate.now());
        turn.setIdentityDocument(IdentityDocument);
        turn.setInitialTime(LocalTime.now());
        turn.setPriority(priority);
        turn.setPatient(name);
        if (role == "student") {
            turn.setRole(UserRol.Student);
        }
        else if (role == "teacher") {
            turn.setRole(UserRol.Teacher);
        }
        else if (role == "admin") {
            turn.setRole(UserRol.Administrative);
        }
        else {
            turn.setRole(UserRol.GeneralServices);
        }
        if (specialization == "Psychology") {
            turn.setSpecialization(Specialization.Psychology);
        }
        else if (specialization == "General Medicine") {
            turn.setSpecialization(Specialization.GeneralMedicine);
        }
        else {
            turn.setSpecialization(Specialization.Dentistry);
        }
        turn.setStatus("Active");
        String Code = createCode(specialization);
        turn.setCode(Code);
        turnRepository.save(turn);
        return Code;
    }

    private String createCode(String specialization) {
        String prefix;
        Specialization specEnum;

        switch (specialization) {
            case "Psychology":
                prefix = "P-";
                specEnum = Specialization.Psychology;
                break;
            case "General Medicine":
                prefix = "M-";
                specEnum = Specialization.GeneralMedicine;
                break;
            default:
                prefix = "O-";
                specEnum = Specialization.Dentistry;
                break;
        }
        long countToday = turnRepository.countBySpecializationAndDate(specEnum, LocalDate.now());

        return prefix + (countToday + 1);
    }

    public void DisableTurns(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psychology":
                specEnum = Specialization.Psychology;
                break;
            case "General Medicine":
                specEnum = Specialization.GeneralMedicine;
                break;
            default:
                specEnum = Specialization.Dentistry;
                break;
        }
        List<Turn> turns = turnRepository.findBySpecializationAndDateAndStatus(
                specEnum, LocalDate.now(), "Active");

        for (Turn turn : turns) {
            turn.setStatus("Disabled");
        }

        turnRepository.saveAll(turns);
    }

    public void PassTurn(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psychology":
                specEnum = Specialization.Psychology;
                break;
            case "General Medicine":
                specEnum = Specialization.GeneralMedicine;
                break;
            default:
                specEnum = Specialization.Dentistry;
                break;
        }

        Optional<Turn> actualTurn = turnRepository.findFirstBySpecializationAndStatus(specEnum,"Attending");
        actualTurn.get().setStatus("Passed");
        actualTurn.get().setFinalTime(LocalTime.now());
        turnRepository.save(actualTurn.get());

        Optional<Turn> turnoPrioritario = turnRepository
                .findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                        specEnum, LocalDate.now(), "Active");

        if (turnoPrioritario.isPresent()) {
            Turn turn = turnoPrioritario.get();
            turn.setStatus("Attending");
            turn.setAttendedTime(LocalTime.now());
            turnRepository.save(turn);
            return;
        }

        Optional<Turn> turnoNormal = turnRepository
                .findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                        specEnum, LocalDate.now(), "Active");

        turnoNormal.ifPresent(turn -> {
            turn.setStatus("Attending");
            turn.setAttendedTime(LocalTime.now());
            turnRepository.save(turn);
        });
    }

    public List<Turn> getNextTurns(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psychology":
                specEnum = Specialization.Psychology;
                break;
            case "General Medicine":
                specEnum = Specialization.GeneralMedicine;
                break;
            default:
                specEnum = Specialization.Dentistry;
                break;
        }

        List<Turn> activeTurns = turnRepository.findBySpecializationAndDateAndStatus(
                specEnum, LocalDate.now(), "Active");

        return activeTurns.stream()
                .sorted(Comparator.comparing(Turn::getPriority).reversed()
                        .thenComparing(Turn::getInitialTime))
                .toList();
    }
    public Report generateReport(LocalDate from, LocalDate to, UserRol rolFilter) {
        List<Turn> allTurns = turnRepository.findByDateBetween(from, to);

        if (rolFilter != null) {
            allTurns = allTurns.stream()
                    .filter(t -> t.getRole() == rolFilter)
                    .toList();
        }

        int total = allTurns.size();
        int completed = (int) allTurns.stream()
                .filter(t -> "Passed".equalsIgnoreCase(t.getStatus()))
                .count();

        List<Turn> completedTurns = allTurns.stream()
                .filter(t -> "Passed".equalsIgnoreCase(t.getStatus()))
                .toList();

        // Tiempo promedio de espera = start - initial
        long totalWaitSeconds = completedTurns.stream()
                .mapToLong(t -> Duration.between(t.getInitialTime(), t.getAttendedTime()).getSeconds())
                .sum();

        // Tiempo promedio de atención = end - start
        long totalAttentionSeconds = completedTurns.stream()
                .mapToLong(t -> Duration.between(t.getAttendedTime(), t.getFinalTime()).getSeconds())
                .sum();

        LocalTime avgWait = totalWaitSeconds == 0 ? LocalTime.MIDNIGHT :
                LocalTime.ofSecondOfDay(totalWaitSeconds / Math.max(completedTurns.size(), 1));

        LocalTime avgAttention = totalAttentionSeconds == 0 ? LocalTime.MIDNIGHT :
                LocalTime.ofSecondOfDay(totalAttentionSeconds / Math.max(completedTurns.size(), 1));

        Report report = new Report();
        report.setActualDate(LocalDate.now());
        report.setActualTime(LocalTime.now());
        report.setInitialDate(from);
        report.setFinalDate(to);
        report.setTotalTurns(total);
        report.setTurnsCompleted(completed);
        report.setAvarageWaitingTime(avgWait);
        report.setAverageTimeAttention(avgAttention);

        setPercentages(report, allTurns, completedTurns);

        return reportRepository.save(report);
    }

    private void setPercentages(Report report, List<Turn> all, List<Turn> completed) {
        Map<UserRol, Long> totalByRole = all.stream()
                .collect(Collectors.groupingBy(Turn::getRole, Collectors.counting()));
        Map<UserRol, Long> completedByRole = completed.stream()
                .collect(Collectors.groupingBy(Turn::getRole, Collectors.counting()));
        Map<UserRol, Long> priorityByRole = all.stream()
                .filter(Turn::getPriority)
                .collect(Collectors.groupingBy(Turn::getRole, Collectors.counting()));

        Map<Specialization, Long> totalBySpec = all.stream()
                .collect(Collectors.groupingBy(Turn::getSpecialization, Collectors.counting()));
        Map<Specialization, Long> completedBySpec = completed.stream()
                .collect(Collectors.groupingBy(Turn::getSpecialization, Collectors.counting()));

        int total = all.size();

        report.setPercentStudents(percent(totalByRole.get(UserRol.Student), total));
        report.setPercentStudentsCompleted(percent(completedByRole.get(UserRol.Student), total));
        report.setPercentStudentsPriority(percent(priorityByRole.get(UserRol.Student), total));

        report.setPercentTeachers(percent(totalByRole.get(UserRol.Teacher), total));
        report.setPercentTeachersCompleted(percent(completedByRole.get(UserRol.Teacher), total));
        report.setPercentTeachersPriority(percent(priorityByRole.get(UserRol.Teacher), total));

        report.setPercentAdmins(percent(totalByRole.get(UserRol.Administrative), total));
        report.setPercentAdminsCompleted(percent(completedByRole.get(UserRol.Administrative), total));
        report.setPercentAdminsPriority(percent(priorityByRole.get(UserRol.Administrative), total));

        report.setPercentGeneralServices(percent(totalByRole.get(UserRol.GeneralServices), total));
        report.setPercentGeneralServicesCompleted(percent(completedByRole.get(UserRol.GeneralServices), total));
        report.setPercentGeneralServicesPriority(percent(priorityByRole.get(UserRol.GeneralServices), total));

        report.setPercentOdontology(percent(totalBySpec.get(Specialization.Dentistry), total));
        report.setPercentOdontologyCompleted(percent(completedBySpec.get(Specialization.Dentistry), total));
        report.setPercentMedicine(percent(totalBySpec.get(Specialization.GeneralMedicine), total));
        report.setPercentMedicineCompleted(percent(completedBySpec.get(Specialization.GeneralMedicine), total));
        report.setPercentPsychology(percent(totalBySpec.get(Specialization.Psychology), total));
        report.setPercentPsychologyCompleted(percent(completedBySpec.get(Specialization.Psychology), total));
    }

    private int percent(Long value, int total) {
        return value == null || total == 0 ? 0 : (int) ((value * 100) / total);
    }

}
