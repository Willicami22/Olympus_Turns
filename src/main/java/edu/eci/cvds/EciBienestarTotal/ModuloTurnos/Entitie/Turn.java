package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Entitie;

import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "Turn")
@Schema(description = "Entidad que representa un turno dentro del sistema")
public class Turn {

    @Id
    @Schema(description = "Identificador único del turno", example = "663c9fa198a432000a4e568a")
    private String id;

    @Schema(description = "Nombre del paciente asociado al turno", example = "Juan Pérez")
    private String patient;

    @Schema(description = "Fecha en la que se agendó el turno", example = "2025-05-08")
    private LocalDate date;

    @Schema(description = "Hora inicial programada para el turno", example = "09:00:00")
    private LocalTime initialTime;

    @Schema(description = "Hora en la que terminó el turno", example = "09:30:00")
    private LocalTime finalTime;

    @Schema(description = "Hora en la que comenzó la atención del turno", example = "09:05:00")
    private LocalTime attendedTime;

    @Schema(description = "Estado del turno (Ej: Activo, En Atencion, Atendido, Cancelado)", example = "Activo")
    private String status;

    @Schema(description = "Especialización médica asignada al turno")
    private Specialization specialization;

    @Schema(description = "Rol del usuario que solicitó el turno")
    private UserRol role;

    @Schema(description = "Documento de identidad del paciente", example = "1234567890")
    private String identityDocument;

    @Schema(description = "Código de verificación del turno", example = "TURNO-ABC123")
    private String code;

    @Schema(description = "Indica si el turno tiene prioridad", example = "true")
    private Boolean priority;

    @Schema(description = "Tipo de discapacidad del paciente (si aplica)")
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
