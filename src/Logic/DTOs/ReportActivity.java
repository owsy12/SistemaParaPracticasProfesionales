package Logic.DTOs;

import java.util.Objects;

public class ReportActivity {
    private int idReportActivity;
    private int idReport;
    private int idActivity;
    private String activityName;
    private String period;
    private String weeklyPlan;
    private String realWeeks;
    private int advancePercentage;
    private String observations;

    public ReportActivity() {
    }

    public int getIdReportActivity() {
        return idReportActivity;
    }

    public void setIdReportActivity(int idReportActivity) {
        this.idReportActivity = idReportActivity;
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
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getWeeklyPlan() {
        return weeklyPlan;
    }

    public void setWeeklyPlan(String weeklyPlan) {
        this.weeklyPlan = weeklyPlan;
    }

    public String getRealWeeks() {
        return realWeeks;
    }

    public void setRealWeeks(String realWeeks) {
        this.realWeeks = realWeeks;
    }

    public int getAdvancePercentage() {
        return advancePercentage;
    }

    public void setAdvancePercentage(int advancePercentage) {
        this.advancePercentage = advancePercentage;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getPeriodDisplay() {
        String periodDisplay;
        if (period != null) {
            periodDisplay = period;
        } else if (weeklyPlan != null) {
            periodDisplay = "Plan:" + weeklyPlan;
        } else {
            periodDisplay = "";
        }
        return periodDisplay;
    }

    public String getDetailDisplay() {
        String detailDisplay;
        if (advancePercentage > 0) {
            detailDisplay = advancePercentage + "%";
        } else if (observations != null) {
            detailDisplay = observations;
        } else {
            detailDisplay = "";
        }
        return detailDisplay;
    }

    public boolean[] getPlanWeekFlags() {
        return parseRange(weeklyPlan);
    }

    public boolean[] getRealWeekFlags() {
        return parseRange(realWeeks);
    }

    public String planCell(int week) {
        boolean[] w = getPlanWeekFlags();
        String cell = (week >= 1 && week <= 8 && w[week - 1]) ? "X" : "";
        return cell;
    }

    public String realCell(int week) {
        boolean[] w = getRealWeekFlags();
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
        return idReportActivity == other.idReportActivity
                && idReport == other.idReport
                && idActivity == other.idActivity
                && Objects.equals(activityName, other.activityName)
                && Objects.equals(period, other.period)
                && Objects.equals(weeklyPlan, other.weeklyPlan)
                && Objects.equals(realWeeks, other.realWeeks)
                && advancePercentage == other.advancePercentage
                && Objects.equals(observations, other.observations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReportActivity, idReport, idActivity, activityName, period, weeklyPlan, realWeeks, advancePercentage, observations);
    }
}
