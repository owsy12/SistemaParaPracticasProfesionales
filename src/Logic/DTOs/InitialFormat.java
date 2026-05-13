package Logic.DTOs;

import java.time.LocalDate;
import java.util.Date;

public class InitialFormat {
    private int idInitialFormat;
    private  int idIntern;
    private int idProject;
    private String formatType; //support types : Assignment letter, Schedule,
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
}
