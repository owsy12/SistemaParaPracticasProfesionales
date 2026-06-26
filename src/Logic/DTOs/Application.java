package Logic.DTOs;

import java.util.Objects;
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



    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Application other = (Application) object;
        return idApplication == other.idApplication
                && idIntern == other.idIntern
                && Objects.equals(status, other.status)
                && Objects.equals(applicationDate, other.applicationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idApplication, idIntern, status, applicationDate);
    }
}
