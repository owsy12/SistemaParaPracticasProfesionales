package Logic.DTOs;

import java.time.LocalDate;
import java.util.Date;

public class Application {
    private int idApplication;
    private int idIntern;
    private String status;
    private LocalDate applicationDate;



    public Application(int idApplication, int idIntern, String status, LocalDate applicationDate) {
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

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }


}
