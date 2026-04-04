package Logic.DTOs;

import java.util.Date;

public class Assignment {
    private int idAssignment;
    private int idIntern;
    private int idProyect;
    private int idApplication;
    private Date assignmentDate;


    public Assignment(int idAssignment, int idIntern, int idProyect, int idApplication, Date assignmentDate) {
        this.idAssignment = idAssignment;
        this.idIntern = idIntern;
        this.idProyect = idProyect;
        this.idApplication = idApplication;
        this.assignmentDate = assignmentDate;
    }

    public Assignment() {
    }

    public int getIdAssignment() {
        return idAssignment;
    }

    public void setIdAssignment(int idAssignment) {
        this.idAssignment = idAssignment;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public int getIdProyect() {
        return idProyect;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
    }

    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public Date getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(Date assignmentDate) {
        this.assignmentDate = assignmentDate;
    }
}

