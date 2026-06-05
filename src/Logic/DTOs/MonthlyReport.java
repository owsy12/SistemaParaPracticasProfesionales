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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        if (!super.equals(object)) {
            return false;
        }
        MonthlyReport other = (MonthlyReport) object;
        return idMonthlyReport == other.idMonthlyReport
                && java.util.Objects.equals(month, other.month)
                && year == other.year
                && monthlyHours == other.monthlyHours
                && java.util.Objects.equals(block, other.block)
                && java.util.Objects.equals(section, other.section)
                && reportNumber == other.reportNumber;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), idMonthlyReport, month, year, monthlyHours, block, section, reportNumber);
    }
}
