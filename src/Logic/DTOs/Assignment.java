package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDate;
import java.util.Date;

public class Assignment {
    private int idAssignment;
    private int idIntern;
    private int idProject;
    private int idApplication;
    private LocalDate assignmentDate;
    private String status;
    private String assignmentReason;


    public Assignment(int idAssignment, int idIntern, int idProject, int idApplication, LocalDate assignmentDate) {
        this.idAssignment = idAssignment;
        this.idIntern = idIntern;
        this.idProject = idProject;
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

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public LocalDate getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(LocalDate assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignmentReason() {
        return assignmentReason;
    }

    public void setAssignmentReason(String assignmentReason) {
        this.assignmentReason = assignmentReason;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Assignment other = (Assignment) object;
        return idAssignment == other.idAssignment
                && idIntern == other.idIntern
                && idProject == other.idProject
                && idApplication == other.idApplication
                && Objects.equals(assignmentDate, other.assignmentDate)
                && Objects.equals(status, other.status)
                && Objects.equals(assignmentReason, other.assignmentReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAssignment, idIntern, idProject, idApplication, assignmentDate, status, assignmentReason);
    }
}

