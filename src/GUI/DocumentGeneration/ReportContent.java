package GUI.DocumentGeneration;

import Logic.DTOs.ReportActivity;
import Logic.DTOs.ReportDeliverable;

import java.util.List;

public class ReportContent {

    private final List<ReportActivity> activities;
    private final List<ReportDeliverable> deliverables;

    public ReportContent(List<ReportActivity> activities,
                         List<ReportDeliverable> deliverables) {
        this.activities = activities;
        this.deliverables = deliverables;
    }

    public List<ReportActivity> getActivities() { return activities; }
    public List<ReportDeliverable> getDeliverables() { return deliverables; }
}
