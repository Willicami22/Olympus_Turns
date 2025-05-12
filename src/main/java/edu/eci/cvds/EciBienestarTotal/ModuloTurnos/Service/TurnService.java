package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO.TurnDTO;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Report;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TurnService {
    @Autowired
    private TurnRepository turnRepository;
    @Autowired
    private ReportRepository reportRepository;

    public String createTurn(String name, String identityDocument, UserRol role, Specialization specialization, Disabilitie disabilitie) {
        Turn turn = new Turn();
        turn.setDate(LocalDate.now());
        turn.setIdentityDocument(identityDocument);
        turn.setInitialTime(LocalTime.now());
        turn.setPatient(name);
        turn.setDisabilitie(disabilitie);
        turn.setRole(role);
        turn.setSpecialization(specialization);
        if (disabilitie == Disabilitie.NoTiene) {
            turn.setPriority(false);
        }
        else {
            turn.setPriority(true);
        }
        turn.setStatus("Activo");
        String code = createCode(specialization.toString());
        turn.setCode(code);
        turnRepository.save(turn);
        return code;
    }


    private String createCode(String specialization) {
        String prefix;
        Specialization specEnum;

        switch (specialization) {
            case "Psicologia":
                prefix = "P-";
                specEnum = Specialization.Psicologia;
                break;
            case "MedicinaGeneral":
                prefix = "M-";
                specEnum = Specialization.MedicinaGeneral;
                break;
            default:
                prefix = "O-";
                specEnum = Specialization.Odontologia;
                break;
        }
        long countToday = turnRepository.countBySpecializationAndDate(specEnum, LocalDate.now());

        return prefix + (countToday + 1);
    }

    public void DisableTurns(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psicologia":
                specEnum = Specialization.Psicologia;
                break;
            case "MedicinaGeneral":
                specEnum = Specialization.MedicinaGeneral;
                break;
            default:
                specEnum = Specialization.Odontologia;
                break;
        }
        List<Turn> turns = turnRepository.findBySpecializationAndDateAndStatus(
                specEnum, LocalDate.now(), "Activo");

        for (Turn turn : turns) {
            turn.setStatus("Inhabilitado");
        }

        turnRepository.saveAll(turns);
    }

    public void PassTurn(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psicologia":
                specEnum = Specialization.Psicologia;
                break;
            case "MedicinaGeneral":
                specEnum = Specialization.MedicinaGeneral;
                break;
            default:
                specEnum = Specialization.Odontologia;
                break;
        }

        Optional<Turn> actualTurn = turnRepository.findFirstBySpecializationAndStatus(specEnum, "En Atencion");
        actualTurn.ifPresent(turn -> {
            turn.setStatus("Atendido");
            turn.setFinalTime(LocalTime.now());
            turnRepository.save(turn);
        });

        Optional<Turn> turnoPrioritario = turnRepository
                .findFirstBySpecializationAndDateAndPriorityIsTrueAndStatusOrderByInitialTimeAsc(
                        specEnum, LocalDate.now(), "Activo");

        if (turnoPrioritario.isPresent()) {
            Turn turn = turnoPrioritario.get();
            turn.setStatus("En Atencion");
            turn.setAttendedTime(LocalTime.now());
            turnRepository.save(turn);
            return ;
        }

        Optional<Turn> turnoNormal = turnRepository
                .findFirstBySpecializationAndDateAndPriorityIsFalseAndStatusOrderByInitialTimeAsc(
                        specEnum, LocalDate.now(), "Activo");

        if (turnoNormal.isPresent()) {
            Turn turn = turnoNormal.get();
            turn.setStatus("En Atencion");
            turn.setAttendedTime(LocalTime.now());
            turnRepository.save(turn);
            return;
        }

        throw new NoSuchElementException("No hay turnos disponibles para la especialidad: " + specEnum);
    }


    public List<Turn> getNextTurns(String specialization) {
        Specialization specEnum;

        switch (specialization) {
            case "Psicologia":
                specEnum = Specialization.Psicologia;
                break;
            case "MedicinaGeneral":
                specEnum = Specialization.MedicinaGeneral;
                break;
            default:
                specEnum = Specialization.Odontologia;
                break;
        }

        List<Turn> activeTurns = turnRepository.findBySpecializationAndDateAndStatus(
                specEnum, LocalDate.now(), "Activo");

        return activeTurns.stream()
                .sorted(Comparator.comparing(Turn::getPriority).reversed()
                        .thenComparing(Turn::getInitialTime))
                .toList();
    }

    public Report generateReport(LocalDate from, LocalDate to, String rolFilter) {
        System.out.println("Generando reporte desde " + from + " hasta " + to + ", filtro: " + rolFilter);

        List<Turn> allTurns = turnRepository.findByDateBetween(from, to);
        System.out.println("Total de turnos encontrados: " + allTurns.size());

        if (allTurns.isEmpty()) {
            System.out.println("No se encontraron turnos en el rango de fechas especificado");
        }

        if (rolFilter != null && !rolFilter.trim().isEmpty()) {
            List<Turn> filteredTurns = allTurns.stream()
                    .filter(t -> t.getRole() != null && rolFilter.equalsIgnoreCase(t.getRole().toString()))
                    .collect(Collectors.toList());

            System.out.println("Turnos después de filtrar por " + rolFilter + ": " + filteredTurns.size());
            allTurns = filteredTurns;
        }

        int total = allTurns.size();

        Map<String, Long> statusCounts = allTurns.stream()
                .collect(Collectors.groupingBy(
                        turn -> turn.getStatus() == null ? "NULL" : turn.getStatus().toString(),
                        Collectors.counting()));

        System.out.println("Estados encontrados en los turnos: " + statusCounts);

        List<Turn> completedTurns = allTurns.stream()
                .filter(t -> t.getStatus() != null && "Atendido".equalsIgnoreCase(t.getStatus().toString()))
                .collect(Collectors.toList());

        int completed = completedTurns.size();
        System.out.println("Turnos completados: " + completed);

        long totalWaitSeconds = 0;
        long totalAttentionSeconds = 0;
        int validWaitTimeCount = 0;
        int validAttentionTimeCount = 0;

        for (Turn turn : completedTurns) {
            if (turn.getInitialTime() != null && turn.getAttendedTime() != null) {
                try {
                    long waitSeconds = Duration.between(turn.getInitialTime(), turn.getAttendedTime()).getSeconds();
                    if (waitSeconds >= 0) {  // Evitar valores negativos
                        totalWaitSeconds += waitSeconds;
                        validWaitTimeCount++;
                    } else {
                        System.out.println("Tiempo de espera negativo detectado para turno ID: " + turn.getId());
                    }
                } catch (Exception e) {
                    System.out.println("Error calculando tiempo de espera: " + e.getMessage());
                }
            }

            if (turn.getAttendedTime() != null && turn.getFinalTime() != null) {
                try {
                    long attentionSeconds = Duration.between(turn.getAttendedTime(), turn.getFinalTime()).getSeconds();
                    if (attentionSeconds >= 0) {  // Evitar valores negativos
                        totalAttentionSeconds += attentionSeconds;
                        validAttentionTimeCount++;
                    } else {
                        System.out.println("Tiempo de atención negativo detectado para turno ID: " + turn.getId());
                    }
                } catch (Exception e) {
                    System.out.println("Error calculando tiempo de atención: " + e.getMessage());
                }
            }
        }

        System.out.println("Total de segundos de espera: " + totalWaitSeconds + " para " + validWaitTimeCount + " turnos");
        System.out.println("Total de segundos de atención: " + totalAttentionSeconds + " para " + validAttentionTimeCount + " turnos");

        LocalTime avgWait = validWaitTimeCount > 0 ?
                LocalTime.ofSecondOfDay(totalWaitSeconds / validWaitTimeCount) : LocalTime.MIDNIGHT;

        LocalTime avgAttention = validAttentionTimeCount > 0 ?
                LocalTime.ofSecondOfDay(totalAttentionSeconds / validAttentionTimeCount) : LocalTime.MIDNIGHT;

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
        int totalTurns = all.size();
        System.out.println("Calculando porcentajes para un total de " + totalTurns + " turnos");

        if (totalTurns == 0) {
            System.out.println("No hay turnos para calcular porcentajes");
            return;
        }

        Set<UserRol> existingRoles = all.stream()
                .map(Turn::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        System.out.println("Roles encontrados en los datos: " + existingRoles);

        Map<UserRol, Long> totalByRole = all.stream()
                .filter(t -> t.getRole() != null)
                .collect(Collectors.groupingBy(Turn::getRole, Collectors.counting()));

        Map<UserRol, Long> completedByRole = completed.stream()
                .filter(t -> t.getRole() != null)
                .collect(Collectors.groupingBy(Turn::getRole, Collectors.counting()));

        List<String> discapacidades = List.of("MayorDeEdad", "DisfuncionMotriz", "Embarazo", "Otra", "NoTiene");

        Set<String> existingDisabilities = all.stream()
                .map(t -> t.getDisabilitie() == null ? "NULL" : t.getDisabilitie().toString())
                .collect(Collectors.toSet());

        System.out.println("Tipos de discapacidad encontrados: " + existingDisabilities);

        for (UserRol rol : UserRol.values()) {
            long totalRol = totalByRole.getOrDefault(rol, 0L);
            long completadosRol = completedByRole.getOrDefault(rol, 0L);

            System.out.println("Rol " + rol + ": " + totalRol + " turnos, " + completadosRol + " completados");

            double porcentajeRol = percent(totalRol, totalTurns);
            double porcentajeCompletados = percent(completadosRol, totalTurns);

            System.out.println("  % del total: " + porcentajeRol + "%, % completados: " + porcentajeCompletados + "%");

            report.setTurnPercentageByRole(rol, porcentajeRol);
            report.setCompletedPercentageByRole(rol, porcentajeCompletados);

            if (totalRol == 0) {
                continue;
            }

            for (String discapacidad : discapacidades) {
                long count = all.stream()
                        .filter(t -> t.getRole() == rol &&
                                t.getDisabilitie() != null &&
                                discapacidad.equalsIgnoreCase(t.getDisabilitie().toString()))
                        .count();

                double porcentajeDiscapacidad = percent(count, (int)totalRol);
                System.out.println("  Discapacidad " + discapacidad + ": " + count + " turnos (" + porcentajeDiscapacidad + "%)");

                report.addDisabilityPercentage(rol, discapacidad, porcentajeDiscapacidad);
            }
        }
    }

    private double percent(Long part, int total) {
        if (part == null || total == 0) {
            return 0.0;
        }
        return (double) part * 100.0 / total;
    }


    public Specialization[] getSpecializations(){return Specialization.values();}
    public Disabilitie[] getDisabilities(){return Disabilitie.values();}

    public Specialization getTurnSpecialization(String id) {
        Optional<Turn> turn = turnRepository.findById(id);
        return turn.map(Turn::getSpecialization).orElse(null);
    }

    public TurnDTO getTurnActualTurn(Specialization specialization) {
        List<Turn> turns = turnRepository.findBySpecializationAndDateAndStatus(
                specialization, LocalDate.now(), "En Atencion");

        if (turns.isEmpty()) {
            return null;
        }
        Turn actualTurn = turns.get(0);
        return toDTO(actualTurn);
    }
    public TurnDTO toDTO(Turn turn) {    
        TurnDTO turnDTO = new TurnDTO();
        turnDTO.setCode(turn.getCode());
        turnDTO.setUserName(turn.getPatient());
        return turnDTO;
    }
}

