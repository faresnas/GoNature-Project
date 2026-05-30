package common.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Carries data for the two main GoNature reports:
 * <ol>
 *   <li><b>Visit Report</b> — entry times and stay durations, broken down
 *       by visitor type (individual vs. group). Displayed graphically.</li>
 *   <li><b>Cancellation Report</b> — cancelled bookings and no-shows,
 *       with averages and daily distribution.</li>
 * </ol>
 *
 * <p>Both report types share this class; the {@link ReportType} field
 * distinguishes them. Unused fields are left {@code null}.</p>
 *
 * @author Group 6
 * @version 1.0
 */
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifies which kind of report this object holds. */
    public enum ReportType {
        /** Visit entry/stay-duration report. */
        VISITS,
        /** Cancellation and no-show report. */
        CANCELLATIONS
    }

    // ── Common fields ────────────────────────────────────────────────────────

    /** Type of this report. */
    private final ReportType reportType;

    /** ID of the park this report covers. */
    private final int parkId;

    /** Calendar year of the reporting period. */
    private final int year;

    /** Calendar month (1–12) of the reporting period. */
    private final int month;

    // ── Visit Report fields ──────────────────────────────────────────────────

    /**
     * Number of individual visitors per day.
     * Key = day-of-month (1–31), value = visitor count.
     */
    private Map<Integer, Integer> individualVisitorsByDay;

    /**
     * Number of group visitors per day.
     * Key = day-of-month, value = visitor count.
     */
    private Map<Integer, Integer> groupVisitorsByDay;

    /** Total individual visitors for the month. */
    private int totalIndividuals;

    /** Total group visitors for the month. */
    private int totalGroupVisitors;

    // ── Cancellation Report fields ───────────────────────────────────────────

    /**
     * Number of user-initiated cancellations per day.
     * Key = day-of-month, value = cancellation count.
     */
    private Map<Integer, Integer> cancellationsByDay;

    /**
     * Number of auto-cancelled (no-show) bookings per day.
     * Key = day-of-month, value = auto-cancel count.
     */
    private Map<Integer, Integer> noshowsByDay;

    /** Average daily cancellations. */
    private double avgCancellations;

    /** Average daily no-shows. */
    private double avgNoshows;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a Report skeleton; populate the appropriate fields
     * before sending to the client.
     *
     * @param reportType type of report
     * @param parkId     park this report is for
     * @param year       report year
     * @param month      report month (1–12)
     */
    public Report(ReportType reportType, int parkId, int year, int month) {
        this.reportType = reportType;
        this.parkId     = parkId;
        this.year       = year;
        this.month      = month;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /** @return report type */
    public ReportType getReportType() { return reportType; }

    /** @return park ID */
    public int getParkId()  { return parkId; }

    /** @return report year */
    public int getYear()    { return year; }

    /** @return report month (1-12) */
    public int getMonth()   { return month; }

    /** @return individual visitors per day map */
    public Map<Integer, Integer> getIndividualVisitorsByDay() { return individualVisitorsByDay; }

    /** @param m individual visitors per day map */
    public void setIndividualVisitorsByDay(Map<Integer, Integer> m) { this.individualVisitorsByDay = m; }

    /** @return group visitors per day map */
    public Map<Integer, Integer> getGroupVisitorsByDay() { return groupVisitorsByDay; }

    /** @param m group visitors per day map */
    public void setGroupVisitorsByDay(Map<Integer, Integer> m) { this.groupVisitorsByDay = m; }

    /** @return total individual visitors for the month */
    public int getTotalIndividuals() { return totalIndividuals; }

    /** @param n total individual visitors */
    public void setTotalIndividuals(int n) { this.totalIndividuals = n; }

    /** @return total group visitors for the month */
    public int getTotalGroupVisitors() { return totalGroupVisitors; }

    /** @param n total group visitors */
    public void setTotalGroupVisitors(int n) { this.totalGroupVisitors = n; }

    /** @return cancellations per day map */
    public Map<Integer, Integer> getCancellationsByDay() { return cancellationsByDay; }

    /** @param m cancellations per day */
    public void setCancellationsByDay(Map<Integer, Integer> m) { this.cancellationsByDay = m; }

    /** @return no-shows per day map */
    public Map<Integer, Integer> getNoshowsByDay() { return noshowsByDay; }

    /** @param m no-shows per day */
    public void setNoshowsByDay(Map<Integer, Integer> m) { this.noshowsByDay = m; }

    /** @return average daily cancellations */
    public double getAvgCancellations() { return avgCancellations; }

    /** @param avg average cancellations */
    public void setAvgCancellations(double avg) { this.avgCancellations = avg; }

    /** @return average daily no-shows */
    public double getAvgNoshows() { return avgNoshows; }

    /** @param avg average no-shows */
    public void setAvgNoshows(double avg) { this.avgNoshows = avg; }
}