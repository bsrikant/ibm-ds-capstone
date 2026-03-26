package com.spacex.analysis;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides analytical utilities over a collection of {@link SpacexLaunchRecord}s,
 * mirroring the exploratory data analysis performed in the IBM Data Science
 * Capstone Python notebooks.
 */
public class SpacexDataAnalyzer {

    private final List<SpacexLaunchRecord> records;

    public SpacexDataAnalyzer(List<SpacexLaunchRecord> records) {
        if (records == null) {
            throw new IllegalArgumentException("records must not be null");
        }
        this.records = List.copyOf(records);
    }

    /**
     * Returns the overall landing success rate across all records (0.0–1.0).
     * Returns {@code 0.0} when there are no records.
     */
    public double overallSuccessRate() {
        if (records.isEmpty()) {
            return 0.0;
        }
        long successCount = records.stream().filter(SpacexLaunchRecord::isSuccess).count();
        return (double) successCount / records.size();
    }

    /**
     * Returns the landing success rate for a specific launch site.
     * Returns {@code 0.0} when no records exist for the given site.
     */
    public double successRateForSite(String launchSite) {
        List<SpacexLaunchRecord> siteRecords = filterBySite(launchSite);
        if (siteRecords.isEmpty()) {
            return 0.0;
        }
        long successCount = siteRecords.stream().filter(SpacexLaunchRecord::isSuccess).count();
        return (double) successCount / siteRecords.size();
    }

    /**
     * Returns a map of launch-site name → success rate (0.0–1.0) for every
     * site present in the data set.
     */
    public Map<String, Double> successRatePerSite() {
        return records.stream()
                .collect(Collectors.groupingBy(SpacexLaunchRecord::getLaunchSite))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<SpacexLaunchRecord> siteRecords = e.getValue();
                            long successes = siteRecords.stream()
                                    .filter(SpacexLaunchRecord::isSuccess).count();
                            return (double) successes / siteRecords.size();
                        }
                ));
    }

    /**
     * Returns payload mass statistics (count, min, max, average, sum) for all
     * records whose payload mass falls within {@code [minKg, maxKg]}.
     */
    public DoubleSummaryStatistics payloadStats(double minKg, double maxKg) {
        return records.stream()
                .filter(r -> r.getPayloadMassKg() >= minKg && r.getPayloadMassKg() <= maxKg)
                .mapToDouble(SpacexLaunchRecord::getPayloadMassKg)
                .summaryStatistics();
    }

    /**
     * Returns all records for a specific launch site, sorted by flight number.
     */
    public List<SpacexLaunchRecord> filterBySite(String launchSite) {
        return records.stream()
                .filter(r -> r.getLaunchSite().equalsIgnoreCase(launchSite))
                .sorted(Comparator.comparingInt(SpacexLaunchRecord::getFlightNumber))
                .collect(Collectors.toList());
    }

    /**
     * Returns all records for a specific booster version category, sorted by
     * flight number.
     */
    public List<SpacexLaunchRecord> filterByBooster(String boosterVersionCategory) {
        return records.stream()
                .filter(r -> r.getBoosterVersionCategory()
                        .equalsIgnoreCase(boosterVersionCategory))
                .sorted(Comparator.comparingInt(SpacexLaunchRecord::getFlightNumber))
                .collect(Collectors.toList());
    }

    /**
     * Returns all records whose payload mass is within the given range
     * {@code [minKg, maxKg]}, optionally restricted to a single launch site.
     * Pass {@code null} for {@code launchSite} to include all sites.
     */
    public List<SpacexLaunchRecord> filterByPayloadRange(double minKg, double maxKg,
                                                          String launchSite) {
        return records.stream()
                .filter(r -> r.getPayloadMassKg() >= minKg && r.getPayloadMassKg() <= maxKg)
                .filter(r -> launchSite == null
                        || r.getLaunchSite().equalsIgnoreCase(launchSite))
                .sorted(Comparator.comparingDouble(SpacexLaunchRecord::getPayloadMassKg))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of records in this analyzer.
     */
    public int totalRecords() {
        return records.size();
    }
}
