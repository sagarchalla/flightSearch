package models;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FlightSearchForm {

    public String getFromDestinations() {
        return fromDestinations;
    }

    public void setFromDestinations(String fromDestination) {
        this.fromDestinations = fromDestination;
    }

    public String getToDestinations() {
        return toDestinations;
    }

    public void setToDestinations(String toDestination) {
        this.toDestinations = toDestination;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    private String fromDestinations;
    private String toDestinations;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date toDate;

}
