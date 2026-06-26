package Logic.DTOs;

import java.util.Objects;

public class ReportActivity {
    private int idReporteActividad;
    private int idReport;
    private int idActivity;
    private String activityName;
    private String periodo;
    private String planSemanas;
    private String realSemanas;
    private int advancePercentage;
    private String observaciones;

    public ReportActivity() {
    }

    public int getIdReporteActividad() {
        return idReporteActividad;
    }

    public void setIdReporteActividad(int idReporteActividad) {
        this.idReporteActividad = idReporteActividad;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getPeriod() {
        return periodo;
    }

    public void setPeriod(String periodo) {
        this.periodo = periodo;
    }

    public String getPlanSemanas() {
        return planSemanas;
    }

    public void setWeeklyPlan(String planSemanas) {
        this.planSemanas = planSemanas;
    }

    public String getRealSemanas() {
        return realSemanas;
    }

    public void setRealWeeks(String realSemanas) {
        this.realSemanas = realSemanas;
    }

    public int getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(int advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservation(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPeriodDisplay() {
        String periodDisplay;
        if (periodo != null) {
            periodDisplay = periodo;
        } else if (planSemanas != null) {
            periodDisplay = "Plan:" + planSemanas;
        } else {
            periodDisplay = "";
        }
        return periodDisplay;
    }

    public String getDetailDisplay() {
        String detailDisplay;
        if (advancePercentage > 0) {
            detailDisplay = advancePercentage + "%";
        } else if (observaciones != null) {
            detailDisplay = observaciones;
        } else {
            detailDisplay = "";
        }
        return detailDisplay;
    }

    public boolean[] getPlanWeeks() {
        return parseRange(planSemanas);
    }

    public boolean[] getRealWeeks() {
        return parseRange(realSemanas);
    }

    public String planCell(int week) {
        boolean[] w = getPlanWeeks();
        String cell = (week >= 1 && week <= 8 && w[week - 1]) ? "X" : "";
        return cell;
    }

    public String realCell(int week) {
        boolean[] w = getRealWeeks();
        String cell = (week >= 1 && week <= 8 && w[week - 1]) ? "X" : "";
        return cell;
    }

    private boolean[] parseRange(String range) {
        boolean[] weeks = new boolean[8];
        if (range != null && !range.isBlank()) {
            String[] parts = range.split(":");
            if (parts.length == 2) {
                try {
                    int from = Integer.parseInt(parts[0].trim());
                    int to = Integer.parseInt(parts[1].trim());
                    for (int i = from; i <= to && i <= 8; i++) {
                        if (i >= 1) {
                            weeks[i - 1] = true;
                        }
                    }
                } catch (NumberFormatException numberFormatException) {
                    weeks = new boolean[8];
                }
            }
        }
        return weeks;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ReportActivity other = (ReportActivity) object;
        return idReporteActividad == other.idReporteActividad
                && idReport == other.idReport
                && idActivity == other.idActivity
                && Objects.equals(activityName, other.activityName)
                && Objects.equals(periodo, other.periodo)
                && Objects.equals(planSemanas, other.planSemanas)
                && Objects.equals(realSemanas, other.realSemanas)
                && advancePercentage == other.advancePercentage
                && Objects.equals(observaciones, other.observaciones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReporteActividad, idReport, idActivity, activityName, periodo, planSemanas, realSemanas, advancePercentage, observaciones);
    }
}
