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
    private int totalTurns;
    private int turnsCompleted;
    private LocalTime AvarageWaitingTime;
    private LocalTime AverageTimeAttention;
    private int percentStudents;
    private int percentStudentsCompleted;
    private int percentStudentsPriority;
    private int percentTeachers;
    private int percentTeachersCompleted;
    private int percentTeachersPriority;
    private int percentAdmins;
    private int percentAdminsCompleted;
    private int percentAdminsPriority;
    private int percentGeneralServices;
    private int percentGeneralServicesCompleted;
    private int percentGeneralServicesPriority;
    private int percentOdontology;
    private int percentOdontologyCompleted;
    private int percentMedicine;
    private int percentMedicineCompleted;
    private int percentPsychology;
    private int percentPsychologyCompleted;


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
    public void setAverageTimeAttention(LocalTime AverageTimeAttention) {this.AverageTimeAttention = AverageTimeAttention;}
    public int getPercentAdmins() {return percentAdmins;    }
    public void setPercentAdmins(int percentAdmins) {this.percentAdmins = percentAdmins;    }
    public int getPercentAdminsCompleted() {return percentAdminsCompleted;}
    public void setPercentAdminsCompleted(int percentAdminsCompleted) {this.percentAdminsCompleted = percentAdminsCompleted;}
    public int getPercentGeneralServices() {return percentGeneralServices;}
    public void setPercentGeneralServices(int percentGeneralServices) {this.percentGeneralServices = percentGeneralServices;}
    public int getPercentGeneralServicesCompleted() {return percentGeneralServicesCompleted;}
    public void setPercentGeneralServicesCompleted(int percentGeneralServicesCompleted) {this.percentGeneralServicesCompleted = percentGeneralServicesCompleted;}
    public int getPercentTeachers() {return percentTeachers;}
    public void setPercentTeachers(int percentTeachers) {this.percentTeachers = percentTeachers;}
    public int getPercentTeachersCompleted() {return percentTeachersCompleted;}
    public void setPercentTeachersCompleted(int percentTeachersCompleted) {this.percentTeachersCompleted = percentTeachersCompleted;}
    public int getPercentStudents() {return percentStudents;}
    public void setPercentStudents(int percentStudents) {this.percentStudents = percentStudents;}
    public int getPercentStudentsCompleted() {return percentStudentsCompleted;  }
    public void setPercentStudentsCompleted(int percentStudentsCompleted) {this.percentStudentsCompleted = percentStudentsCompleted;}
    public int getPercentMedicineCompleted() {return percentMedicineCompleted;}
    public void setPercentMedicineCompleted(int percentMedicineCompleted) {this.percentMedicineCompleted = percentMedicineCompleted;}
    public int getPercentPsychologyCompleted() {return percentPsychologyCompleted;}
    public void setPercentPsychologyCompleted(int percentPsychologyCompleted) {this.percentPsychologyCompleted = percentPsychologyCompleted;}
    public int getPercentOdontology() {return percentOdontology;}
    public int getPercentOdontologyCompleted() {return percentOdontologyCompleted;}
    public int getPercentPsychology() {return percentPsychology;}
    public void setPercentPsychology(int percentPsychology) {this.percentPsychology = percentPsychology;}
    public void setPercentMedicine(int percentMedicine) {this.percentMedicine = percentMedicine;}
    public int getPercentMedicine() {return percentMedicine;}
    public void setPercentOdontology(int percentOdontology) {this.percentOdontology = percentOdontology;}
    public void setPercentOdontologyCompleted(int percentOdontologyCompleted) {this.percentOdontologyCompleted = percentOdontologyCompleted;   }
    public int getPercentAdminsPriority() {return percentAdminsPriority;}
    public int getPercentGeneralServicesPriority() {return percentGeneralServicesPriority;}
    public int getPercentStudentsPriority() {return percentStudentsPriority;}
    public void setPercentStudentsPriority(int percentStudentsPriority) {this.percentStudentsPriority = percentStudentsPriority;}
    public int getPercentTeachersPriority() {return percentTeachersPriority;}
    public void setPercentTeachersPriority(int percentTeachersPriority) {this.percentTeachersPriority = percentTeachersPriority;}
    public void setPercentAdminsPriority(int percentAdminsPriority) {this.percentAdminsPriority = percentAdminsPriority;}
    public void setPercentGeneralServicesPriority(int percentGeneralServicesPriority) {this.percentGeneralServicesPriority = percentGeneralServicesPriority;}
}
