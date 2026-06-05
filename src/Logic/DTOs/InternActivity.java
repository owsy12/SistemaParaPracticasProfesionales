package Logic.DTOs;

import java.time.LocalDate;

public class InternActivity {
    private int idInternActivity;
    private int idActivity;
    private int idIntern;
    private int dedicatedHours;
    private String status;
    private LocalDate completionDate;
    private String observations;
    private String activityName;

    public InternActivity(int idInternActivity, int idActivity, int idIntern,
                          int dedicatedHours, String status,
                          LocalDate completionDate, String observations) {
        this.idInternActivity = idInternActivity;
        this.idActivity = idActivity;
        this.idIntern = idIntern;
        this.dedicatedHours = dedicatedHours;
        this.status = status;
        this.completionDate = completionDate;
        this.observations = observations;
    }

    public InternActivity() {
    }

    public int getIdInternActivity() {
        return idInternActivity;
    }

    public void setIdInternActivity(int idInternActivity) {
        this.idInternActivity = idInternActivity;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public int getIdIntern() {
        return idIntern;
    }

    public void setIdIntern(int idIntern) {
        this.idIntern = idIntern;
    }

    public int getDedicatedHours() {
        return dedicatedHours;
    }

    public void setDedicatedHours(int dedicatedHours) {
        this.dedicatedHours = dedicatedHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDedicatedHoursDisplay() {
        String display = String.valueOf(dedicatedHours);
        return display;
    }

    @Override
    public String toString() {
        return activityName != null ? activityName : String.valueOf(idActivity);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        InternActivity other = (InternActivity) object;
        return idInternActivity == other.idInternActivity
                && idActivity == other.idActivity
                && idIntern == other.idIntern
                && dedicatedHours == other.dedicatedHours
                && java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(completionDate, other.completionDate)
                && java.util.Objects.equals(observations, other.observations)
                && java.util.Objects.equals(activityName, other.activityName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idInternActivity, idActivity, idIntern, dedicatedHours, status, completionDate, observations, activityName);
    }
}
