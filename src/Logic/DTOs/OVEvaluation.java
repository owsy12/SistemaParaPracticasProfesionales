package Logic.DTOs;

import java.time.LocalDateTime;

public class OVEvaluation {

    private int idOVEvaluation;
    private int idIntern;
    private int idProject;
    private String documentPath;
    private String status;
    private LocalDateTime deliveryDate;

    public OVEvaluation() {
    }

    public int getIdOVEvaluation() {
        return idOVEvaluation;
    }

    public void setIdOVEvaluation(int idOVEvaluation) {
        this.idOVEvaluation = idOVEvaluation;
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

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
