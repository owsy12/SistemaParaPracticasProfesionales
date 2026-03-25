package Logic.DTOs;

import java.util.Date;

public class Proyect {
    private int idProyect;
    private int idLinkedOrganization;
    private LinkedOrganization linkedOrganization;
    private String name;
    private String generalObject;
    private String methodology;
    private String maximumPlaces;
    private String state;
    private Date insertDate;

    public Proyect() {
    }

    public Proyect(int idProyect, int idLinkedOrganization, LinkedOrganization linkedOrganization, String name, String generalObject, String methodology, String maximumPlaces, String state, Date insertDate) {
        this.idProyect = idProyect;
        this.idLinkedOrganization = idLinkedOrganization;
        this.linkedOrganization = linkedOrganization;
        this.name = name;
        this.generalObject = generalObject;
        this.methodology = methodology;
        this.maximumPlaces = maximumPlaces;
        this.state = state;
        this.insertDate = insertDate;
    }


    public int getIdProyect() {
        return idProyect;
    }

    public int getIdLinkedOrganization() {
        return idLinkedOrganization;
    }

    public LinkedOrganization getLinkedOrganization() {
        return linkedOrganization;
    }

    public String getName() {
        return name;
    }

    public String getGeneralObject() {
        return generalObject;
    }

    public String getMethodology() {
        return methodology;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMaximumPlaces() {
        return maximumPlaces;
    }

    public void setMaximumPlaces(String maximumPlaces) {
        this.maximumPlaces = maximumPlaces;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public void setGeneralObject(String generalObject) {
        this.generalObject = generalObject;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLinkedOrganization(LinkedOrganization linkedOrganization) {
        this.linkedOrganization = linkedOrganization;
    }

    public void setIdLinkedOrganization(int idLinkedOrganization) {
        this.idLinkedOrganization = idLinkedOrganization;
    }

    public void setIdProyect(int idProyect) {
        this.idProyect = idProyect;
    }
}
