package Logic.DTOs;

import java.util.Objects;
import java.time.LocalDateTime;

public class LinkedOrganizationEvaluation {

    private int idLinkedOrganizationEvaluation;
    private int idIntern;
    private int idProject;
    private String documentPath;
    private String status;
    private LocalDateTime deliveryDate;

    public LinkedOrganizationEvaluation() {
    }

    public int getIdLinkedOrganizationEvaluation() {
        return idLinkedOrganizationEvaluation;
    }

    public void setIdLinkedOrganizationEvaluation(int idLinkedOrganizationEvaluation) {
        this.idLinkedOrganizationEvaluation = idLinkedOrganizationEvaluation;
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
        LinkedOrganizationEvaluation other = (LinkedOrganizationEvaluation) object;
        return idLinkedOrganizationEvaluation == other.idLinkedOrganizationEvaluation
                && idIntern == other.idIntern
                && idProject == other.idProject
                && Objects.equals(documentPath, other.documentPath)
                && Objects.equals(status, other.status)
                && Objects.equals(deliveryDate, other.deliveryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idLinkedOrganizationEvaluation, idIntern, idProject, documentPath, status, deliveryDate);
    }
}
