package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDate;
import java.util.Date;

public class InitialFormat {
    private int idInitialFormat;
    private int idIntern;
    private int idProject;
    private String formatType;
    private String filePath;
    private String status;
    private LocalDate submissionDate;

    public InitialFormat(int idInitialFormat, int idIntern, String formatType, String filePath, String status, LocalDate submissionDate) {
        this.idInitialFormat = idInitialFormat;
        this.idIntern = idIntern;
        this.formatType = formatType;
        this.filePath = filePath;
        this.status = status;
        this.submissionDate = submissionDate;
    }

    public InitialFormat() {
    }

    public int getIdInitialFormat() {
        return idInitialFormat;
    }

    public void setIdInitialFormat(int idInitialFormat) {
        this.idInitialFormat = idInitialFormat;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public String getFormatType() {
        return formatType;
    }

    public void setFormatType(String formatType) {
        this.formatType = formatType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(LocalDate submissionDate) {
        this.submissionDate = submissionDate;
    }

    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        InitialFormat other = (InitialFormat) object;
        return idInitialFormat == other.idInitialFormat
                && idIntern == other.idIntern
                && idProject == other.idProject
                && Objects.equals(formatType, other.formatType)
                && Objects.equals(filePath, other.filePath)
                && Objects.equals(status, other.status)
                && Objects.equals(submissionDate, other.submissionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInitialFormat, idIntern, idProject, formatType, filePath, status, submissionDate);
    }
}
