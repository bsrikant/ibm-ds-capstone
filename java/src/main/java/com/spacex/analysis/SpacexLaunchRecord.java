package com.spacex.analysis;

import java.time.LocalDate;

/**
 * Represents a single SpaceX launch record with relevant fields used in
 * the IBM Data Science Capstone analysis.
 */
public class SpacexLaunchRecord {

    private int flightNumber;
    private LocalDate date;
    private String launchSite;
    private double payloadMassKg;
    private String boosterVersionCategory;
    /** 1 = successful landing, 0 = failed landing */
    private int landingOutcome;

    public SpacexLaunchRecord(int flightNumber, LocalDate date, String launchSite,
                              double payloadMassKg, String boosterVersionCategory,
                              int landingOutcome) {
        if (launchSite == null || launchSite.isBlank()) {
            throw new IllegalArgumentException("launchSite must not be null or blank");
        }
        if (boosterVersionCategory == null || boosterVersionCategory.isBlank()) {
            throw new IllegalArgumentException("boosterVersionCategory must not be null or blank");
        }
        if (payloadMassKg < 0) {
            throw new IllegalArgumentException("payloadMassKg must be non-negative");
        }
        if (landingOutcome != 0 && landingOutcome != 1) {
            throw new IllegalArgumentException("landingOutcome must be 0 or 1");
        }
        this.flightNumber = flightNumber;
        this.date = date;
        this.launchSite = launchSite;
        this.payloadMassKg = payloadMassKg;
        this.boosterVersionCategory = boosterVersionCategory;
        this.landingOutcome = landingOutcome;
    }

    public int getFlightNumber() {
        return flightNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getLaunchSite() {
        return launchSite;
    }

    public double getPayloadMassKg() {
        return payloadMassKg;
    }

    public String getBoosterVersionCategory() {
        return boosterVersionCategory;
    }

    public int getLandingOutcome() {
        return landingOutcome;
    }

    public boolean isSuccess() {
        return landingOutcome == 1;
    }

    @Override
    public String toString() {
        return String.format("SpacexLaunchRecord{flight=%d, date=%s, site='%s', "
                + "payload=%.1f kg, booster='%s', outcome=%s}",
                flightNumber, date, launchSite, payloadMassKg,
                boosterVersionCategory, isSuccess() ? "SUCCESS" : "FAILURE");
    }
}
