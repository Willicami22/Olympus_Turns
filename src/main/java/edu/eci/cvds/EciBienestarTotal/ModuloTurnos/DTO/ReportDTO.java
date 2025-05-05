package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReportDTO {
    private LocalDate ActualDate;
    private LocalDate initialDate;
    private LocalDate finalDate;
    private String userRole;
    private int totalTurns;
    private int turnsCompleted;
    private LocalTime AvarageWaitingTime;
    private LocalTime AverageTimeAttention;

}
