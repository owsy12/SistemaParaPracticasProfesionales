package Logic.DTOs;

public class SelfEvaluation {
    private int idSelfEvalation;
    private int idIntern;
    private int idProject;
    private String period;
    private int statement01;
    private int statement02;
    private int statement03;
    private int statement04;
    private int statement05;
    private int statement06;
    private int statement07;
    private int statement08;
    private int statement09;
    private int statement10;
    private int finalScore;
    private String placeAndDate;
    private String documentPath;
    private String status;


    public SelfEvaluation(int idSelfEvalation, int idIntern, int idProject, String period, int statement01, int statement02, int statement03, int statement04, int statement05, int statement06, int statement07, int statement08, int statement09, int statement10) {
        this.idSelfEvalation = idSelfEvalation;
        this.idIntern = idIntern;
        this.idProject = idProject;
        this.period = period;
        this.statement01 = statement01;
        this.statement02 = statement02;
        this.statement03 = statement03;
        this.statement04 = statement04;
        this.statement05 = statement05;
        this.statement06 = statement06;
        this.statement07 = statement07;
        this.statement08 = statement08;
        this.statement09 = statement09;
        this.statement10 = statement10;
    }

    public SelfEvaluation() {
    }

    public int getIdSelfEvalation() {
        return idSelfEvalation;
    }

    public void setIdSelfEvalation(int idSelfEvalation) {
        this.idSelfEvalation = idSelfEvalation;
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getStatement01() {
        return statement01;
    }

    public void setStatement01(int statement01) {
        this.statement01 = statement01;
    }

    public int getStatement02() {
        return statement02;
    }

    public void setStatement02(int statement02) {
        this.statement02 = statement02;
    }

    public int getStatement03() {
        return statement03;
    }

    public void setStatement03(int statement03) {
        this.statement03 = statement03;
    }

    public int getStatement04() {
        return statement04;
    }

    public void setStatement04(int statement04) {
        this.statement04 = statement04;
    }

    public int getStatement05() {
        return statement05;
    }

    public void setStatement05(int statement05) {
        this.statement05 = statement05;
    }

    public int getStatement06() {
        return statement06;
    }

    public void setStatement06(int statement06) {
        this.statement06 = statement06;
    }

    public int getStatement07() {
        return statement07;
    }

    public void setStatement07(int statement07) {
        this.statement07 = statement07;
    }

    public int getStatement08() {
        return statement08;
    }

    public void setStatement08(int statement08) {
        this.statement08 = statement08;
    }

    public int getStatement09() {
        return statement09;
    }

    public void setStatement09(int statement09) {
        this.statement09 = statement09;
    }

    public int getStatement10() {
        return statement10;
    }

    public void setStatement10(int statement10) {
        this.statement10 = statement10;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public String getPlaceAndDate() {
        return placeAndDate;
    }

    public void setPlaceAndDate(String placeAndDate) {
        this.placeAndDate = placeAndDate;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        SelfEvaluation other = (SelfEvaluation) object;
        return idSelfEvalation == other.idSelfEvalation
                && idIntern == other.idIntern
                && idProject == other.idProject
                && java.util.Objects.equals(period, other.period)
                && statement01 == other.statement01
                && statement02 == other.statement02
                && statement03 == other.statement03
                && statement04 == other.statement04
                && statement05 == other.statement05
                && statement06 == other.statement06
                && statement07 == other.statement07
                && statement08 == other.statement08
                && statement09 == other.statement09
                && statement10 == other.statement10
                && finalScore == other.finalScore
                && java.util.Objects.equals(placeAndDate, other.placeAndDate)
                && java.util.Objects.equals(documentPath, other.documentPath)
                && java.util.Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(idSelfEvalation, idIntern, idProject, period, statement01, statement02, statement03, statement04, statement05, statement06, statement07, statement08, statement09, statement10, finalScore, placeAndDate, documentPath, status);
    }
}
