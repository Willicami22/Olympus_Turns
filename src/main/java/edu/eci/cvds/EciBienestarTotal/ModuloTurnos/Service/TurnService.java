package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Service;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie.Turn;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.ReportRepository;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Repository.TurnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

}
