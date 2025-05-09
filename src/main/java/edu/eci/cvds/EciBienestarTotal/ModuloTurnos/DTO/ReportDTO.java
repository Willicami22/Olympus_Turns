package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class ReportDTO {
    private LocalDate initialDate;
    private LocalDate finalDate;
    private String userRole;


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
    public String getUserRole() {
        return userRole;
    }
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

}
