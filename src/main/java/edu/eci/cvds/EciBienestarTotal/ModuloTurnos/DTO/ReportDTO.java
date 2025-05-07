package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;

import java.time.LocalDate;

public class ReportDTO {
    private LocalDate initialDate;
    private LocalDate finalDate;
    private UserRol userRole;

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
}

