package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "Turn")
public class Turn {
    @Id
    private String id;
    private String patient;
    private LocalDate date;
    private LocalTime initialTime;
    private LocalTime finalTime;
    private LocalTime attendedTime;
    private String status;
    private Specialization specialization;
    private UserRol role;
    private String identityDocument;
    private String code;
    private Boolean priority;
    private Disabilitie disabilitie;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPatient() {
        return patient;
    }
    public void setPatient(String patient) {
        this.patient = patient;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public LocalTime getInitialTime() {
        return initialTime;
    }
    public void setInitialTime(LocalTime initialTime) {
        this.initialTime = initialTime;
    }
    public LocalTime getFinalTime() {
        return finalTime;
    }
    public void setFinalTime(LocalTime finalTime) {this.finalTime = finalTime;}
    public LocalTime getAttendedTime() {
        return attendedTime;
    }
    public void setAttendedTime(LocalTime attendedTime) {
        this.attendedTime = attendedTime;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdentityDocument() {
        return identityDocument;
    }
    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public Boolean getPriority() {
        return priority;
    }
    public void setPriority(Boolean priority) {this.priority = priority;}

    public Specialization getSpecialization() {
        return specialization;
    }
    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
    public UserRol getRole() {
        return role;
    }
    public void setRole(UserRol role) {
        this.role = role;
    }
    public void setDisabilitie(Disabilitie disabilitie){this.disabilitie = disabilitie;}
    public Disabilitie getDisabilitie(){return this.disabilitie;}
}
