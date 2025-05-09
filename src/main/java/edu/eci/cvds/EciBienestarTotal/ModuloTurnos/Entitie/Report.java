package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "Report")
@Schema(description = "Entidad que representa un reporte de turnos")
public class Report {

    @Id
    @Schema(description = "Identificador único del reporte", example = "663c9f5d98a432000a4e567d")
    private String id;

    @Schema(description = "Fecha en que se generó el reporte", example = "2025-05-08")
    private LocalDate ActualDate;

    @Schema(description = "Hora en que se generó el reporte", example = "15:30:45")
    private LocalTime ActualTime;

    @Schema(description = "Fecha inicial del rango del reporte", example = "2025-05-01")
    private LocalDate initialDate;

    @Schema(description = "Fecha final del rango del reporte", example = "2025-05-07")
    private LocalDate finalDate;

    @Schema(description = "Rol de usuario aplicado como filtro (si aplica)")
    private UserRol userRole;

    @Schema(description = "Cantidad total de turnos en el rango especificado", example = "50")
    private int totalTurns;

    @Schema(description = "Cantidad de turnos completados en el rango", example = "45")
    private int turnsCompleted;

    @Schema(description = "Tiempo promedio de espera", example = "00:10:30")
    private LocalTime AvarageWaitingTime;

    @Schema(description = "Tiempo promedio de atención", example = "00:12:45")
    private LocalTime AverageTimeAttention;

    @Schema(description = "Porcentaje de turnos por rol")
    private Map<UserRol, Double> turnPercentageByRole = new HashMap<>();

    @Schema(description = "Porcentaje de turnos completados por rol")
    private Map<UserRol, Double> completedPercentageByRole = new HashMap<>();

    @Schema(description = "Porcentajes de discapacidades por rol")
    private Map<UserRol, Map<String, Double>> disabilityPercentagesByRole = new HashMap<>();

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
    public Map<UserRol, Double> getTurnPercentageByRole() {
        return turnPercentageByRole;
    }

    public void setTurnPercentageByRole(UserRol role, double percentage) {
        turnPercentageByRole.put(role, percentage);
    }

    public Map<UserRol, Double> getCompletedPercentageByRole() {
        return completedPercentageByRole;
    }

    public void setCompletedPercentageByRole(UserRol role, double percentage) {
        completedPercentageByRole.put(role, percentage);
    }

    public Map<UserRol, Map<String, Double>> getDisabilityPercentagesByRole() {
        return disabilityPercentagesByRole;
    }

    public void addDisabilityPercentage(UserRol role, String disability, double percentage) {
        disabilityPercentagesByRole
                .computeIfAbsent(role, k -> new HashMap<>())
                .put(disability, percentage);
    }
}


