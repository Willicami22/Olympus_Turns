package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "Report")
public class Report {
    @Id
    private String id;
    private LocalDate ActualDate;
    private LocalTime ActualTime;
    private LocalDate initialDate;
    private LocalDate finalDate;
    private UserRol userRole;
    private int totalTurns;
    private int turnsCompleted;
    private LocalTime AvarageWaitingTime;
    private LocalTime AverageTimeAttention;
    private Specialization specialization;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public LocalDate getActualDate() {
        return ActualDate;
    }
    public void setActualDate(LocalDate actualDate) {
        ActualDate = actualDate;
    }
    public LocalTime getActualTime() {
        return ActualTime;
    }
    public void setActualTime(LocalTime actualTime) {
        ActualTime = actualTime;
    }
    public LocalDate getInitialDate() {
        return initialDate;
    }
    public void setInitialDate(LocalDate initialDate) {
        this.initialDate = initialDate;
    }
    public LocalDate getFinalDate() {
        return finalDate;
    }
    public void setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
    }
    public UserRol getUserRole() {
        return userRole;
    }
    public void setUserRole(UserRol userRole) {
        this.userRole = userRole;
    }
    public int getTotalTurns() {
        return totalTurns;
    }
    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }
    public int getTurnsCompleted() {
        return turnsCompleted;
    }
    public void setTurnsCompleted(int turnsCompleted) {
        this.turnsCompleted = turnsCompleted;
    }
    public LocalTime getAvarageWaitingTime() {
        return AvarageWaitingTime;
    }
    public void setAvarageWaitingTime(LocalTime AvarageWaitingTime) {
        this.AvarageWaitingTime = AvarageWaitingTime;
    }
    public LocalTime getAverageTimeAttention() {
        return AverageTimeAttention;
    }
    public void setAverageTimeAttention(LocalTime AverageTimeAttention) {
        this.AverageTimeAttention = AverageTimeAttention;
    }
    public Specialization getSpecialization() {
        return specialization;
    }
    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

}
