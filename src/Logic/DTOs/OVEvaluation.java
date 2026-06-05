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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        OVEvaluation other = (OVEvaluation) object;
        return idOVEvaluation == other.idOVEvaluation
                && idIntern == other.idIntern
                && idProject == other.idProject
                && java.util.Objects.equals(documentPath, other.documentPath)
                && java.util.Objects.equals(status, other.status)
                && java.util.Objects.equals(deliveryDate, other.deliveryDate);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idOVEvaluation, idIntern, idProject, documentPath, status, deliveryDate);
    }
}
