package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TurnDTO {
    private String Code;
    private String userName;
    @JsonProperty("specialization")
    private String specialization;
    private String state;
    private boolean priority;
    private String identityDocument;
    private String role;

    public TurnDTO() {}
    public String getCode() {
        return Code;
    }
    public void setCode(String Code) {
        this.Code = Code;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String speciality) {
        this.specialization = speciality;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public boolean isPriority() {
        return priority;
    }
    public void setPriority(boolean priority) {
        this.priority = priority;
    }
    public String getIdentityDocument() {
        return identityDocument;
    }
    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

}
