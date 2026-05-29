package Logic.DTOs;

public class MonthlyReport extends Report{
    private int idMonthlyReport;
    private String month;
    private int year;
    private int monthlyHours;
    private String block;
    private String section;
    private int reportNumber;

    public MonthlyReport(int idMonthlyReport, String month, int year, String block, String section) {
        this.idMonthlyReport = idMonthlyReport;
        this.month = month;
        this.year = year;
        this.block = block;
        this.section = section;
    }

    public MonthlyReport() {
    }

    public int getIdMonthlyReport() {
        return idMonthlyReport;
    }

    public void setIdMonthlyReport(int idMonthlyReport) {
        this.idMonthlyReport = idMonthlyReport;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getMonthlyHours() {
        return monthlyHours;
    }

    public void setMonthlyHours(int monthlyHours) {
        this.monthlyHours = monthlyHours;
    }

    public int getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(int reportNumber) {
        this.reportNumber = reportNumber;
    }
}
