package Logic.DTOs;

import java.util.Date;

public class Application {
    private int idApplication;
    private int idIntern;
    private String status;
    private Date applicationDate;


    public Application(int idApplication, int idIntern, String status, Date applicationDate) {
        this.idApplication = idApplication;
        this.idIntern = idIntern;
        this.status = status;
        this.applicationDate = applicationDate;
    }

    public Application() {
    }

    public int getIdApplication() {
        return idApplication;
    }

    public void setIdApplication(int idApplication) {
        this.idApplication = idApplication;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }
}
