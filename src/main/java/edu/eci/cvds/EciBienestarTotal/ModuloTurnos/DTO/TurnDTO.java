package edu.eci.cvds.EciBienestarTotal.ModuloTurnos.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Disabilitie;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.Specialization;
import edu.eci.cvds.EciBienestarTotal.ModuloTurnos.Enum.UserRol;

public class TurnDTO {
    private String Code;
    private String userName;
    @JsonProperty("specialization")
    private Specialization specialization;
    private String state;
    private String identityDocument;
    private UserRol role;
    private Disabilitie disabilitie;

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
    public Specialization getSpecialization() {
        return specialization;
    }
    public void setSpecialization(Specialization speciality) {
        this.specialization = speciality;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getIdentityDocument() {
        return identityDocument;
    }
    public void setIdentityDocument(String identityDocument) {
        this.identityDocument = identityDocument;
    }
    public UserRol getRole() {
        return role;
    }
    public void setRole(UserRol role) {
        this.role = role;
    }
    public Disabilitie getDisabilitie(){ return this.disabilitie;}
    public void setDisabilitie(Disabilitie disabilitie){this.disabilitie = disabilitie;}

}
