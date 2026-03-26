package com.spacex.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpacexDataAnalyzerTest {

    private static final String SITE_KSC = "KSC LC-39A";
    private static final String SITE_CCAFS = "CCAFS LC-40";
    private static final String BOOSTER_FT = "FT";
    private static final String BOOSTER_B4 = "B4";

    private SpacexDataAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        List<SpacexLaunchRecord> records = List.of(
                new SpacexLaunchRecord(1, LocalDate.of(2017, 1, 14), SITE_KSC,   2500.0, BOOSTER_FT, 1),
                new SpacexLaunchRecord(2, LocalDate.of(2017, 3, 16), SITE_KSC,   5300.0, BOOSTER_FT, 1),
                new SpacexLaunchRecord(3, LocalDate.of(2017, 5, 1),  SITE_KSC,   3000.0, BOOSTER_B4, 0),
                new SpacexLaunchRecord(4, LocalDate.of(2018, 2, 22), SITE_CCAFS, 4500.0, BOOSTER_FT, 1),
                new SpacexLaunchRecord(5, LocalDate.of(2018, 7, 25), SITE_CCAFS, 7000.0, BOOSTER_B4, 0)
        );
        analyzer = new SpacexDataAnalyzer(records);
    }

    // ── SpacexLaunchRecord tests ────────────────────────────────────────────

    @Test
    void launchRecord_isSuccessReturnsTrueForOutcomeOne() {
        SpacexLaunchRecord r = new SpacexLaunchRecord(10, LocalDate.now(), SITE_KSC, 1000, BOOSTER_FT, 1);
        assertTrue(r.isSuccess());
    }

    @Test
    void launchRecord_isSuccessReturnsFalseForOutcomeZero() {
        SpacexLaunchRecord r = new SpacexLaunchRecord(10, LocalDate.now(), SITE_KSC, 1000, BOOSTER_FT, 0);
        assertFalse(r.isSuccess());
    }

    @Test
    void launchRecord_throwsOnNegativePayload() {
        assertThrows(IllegalArgumentException.class, () ->
                new SpacexLaunchRecord(1, LocalDate.now(), SITE_KSC, -1.0, BOOSTER_FT, 1));
    }

    @Test
    void launchRecord_throwsOnInvalidOutcome() {
        assertThrows(IllegalArgumentException.class, () ->
                new SpacexLaunchRecord(1, LocalDate.now(), SITE_KSC, 1000, BOOSTER_FT, 2));
    }

    @Test
    void launchRecord_throwsOnBlankLaunchSite() {
        assertThrows(IllegalArgumentException.class, () ->
                new SpacexLaunchRecord(1, LocalDate.now(), "  ", 1000, BOOSTER_FT, 1));
    }

    @Test
    void launchRecord_toStringContainsSiteAndOutcome() {
        SpacexLaunchRecord r = new SpacexLaunchRecord(5, LocalDate.of(2021, 6, 3), SITE_KSC, 2000, BOOSTER_FT, 1);
        String s = r.toString();
        assertTrue(s.contains(SITE_KSC));
        assertTrue(s.contains("SUCCESS"));
    }

    // ── SpacexDataAnalyzer tests ────────────────────────────────────────────

    @Test
    void totalRecords_returnsCorrectCount() {
        assertEquals(5, analyzer.totalRecords());
    }

    @Test
    void overallSuccessRate_calculatesCorrectly() {
        // 3 successes out of 5 records
        assertEquals(0.6, analyzer.overallSuccessRate(), 1e-9);
    }

    @Test
    void overallSuccessRate_emptyListReturnsZero() {
        SpacexDataAnalyzer empty = new SpacexDataAnalyzer(List.of());
        assertEquals(0.0, empty.overallSuccessRate(), 1e-9);
    }

    @Test
    void successRateForSite_kscHasTwoOutOfThree() {
        // KSC: flights 1 (success), 2 (success), 3 (failure) → 2/3
        assertEquals(2.0 / 3.0, analyzer.successRateForSite(SITE_KSC), 1e-9);
    }

    @Test
    void successRateForSite_ccafsHasOneOutOfTwo() {
        // CCAFS: flights 4 (success), 5 (failure) → 1/2
        assertEquals(0.5, analyzer.successRateForSite(SITE_CCAFS), 1e-9);
    }

    @Test
    void successRateForSite_unknownSiteReturnsZero() {
        assertEquals(0.0, analyzer.successRateForSite("UNKNOWN SITE"), 1e-9);
    }

    @Test
    void successRatePerSite_containsBothSites() {
        Map<String, Double> rates = analyzer.successRatePerSite();
        assertTrue(rates.containsKey(SITE_KSC));
        assertTrue(rates.containsKey(SITE_CCAFS));
        assertEquals(2.0 / 3.0, rates.get(SITE_KSC), 1e-9);
        assertEquals(0.5, rates.get(SITE_CCAFS), 1e-9);
    }

    @Test
    void filterBySite_returnsOnlySiteRecordsInFlightOrder() {
        List<SpacexLaunchRecord> kscRecords = analyzer.filterBySite(SITE_KSC);
        assertEquals(3, kscRecords.size());
        assertEquals(1, kscRecords.get(0).getFlightNumber());
        assertEquals(2, kscRecords.get(1).getFlightNumber());
        assertEquals(3, kscRecords.get(2).getFlightNumber());
    }

    @Test
    void filterByBooster_returnsCorrectRecords() {
        List<SpacexLaunchRecord> ftRecords = analyzer.filterByBooster(BOOSTER_FT);
        // Flights 1, 2, 4 use FT booster
        assertEquals(3, ftRecords.size());
        assertTrue(ftRecords.stream().allMatch(r -> r.getBoosterVersionCategory().equals(BOOSTER_FT)));
    }

    @Test
    void filterByPayloadRange_allSitesWithinRange() {
        // Payload range 2000–5500 should include flights 1 (2500), 2 (5300), 3 (3000), 4 (4500)
        List<SpacexLaunchRecord> filtered = analyzer.filterByPayloadRange(2000, 5500, null);
        assertEquals(4, filtered.size());
    }

    @Test
    void filterByPayloadRange_specificSite() {
        // KSC, payload 2000–4000: flights 1 (2500) and 3 (3000)
        List<SpacexLaunchRecord> filtered = analyzer.filterByPayloadRange(2000, 4000, SITE_KSC);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(r -> r.getLaunchSite().equals(SITE_KSC)));
    }

    @Test
    void payloadStats_computesCorrectAverage() {
        // All records: payloads 2500, 5300, 3000, 4500, 7000
        var stats = analyzer.payloadStats(0, 10000);
        assertEquals(5, stats.getCount());
        assertEquals(2500.0, stats.getMin(), 1e-9);
        assertEquals(7000.0, stats.getMax(), 1e-9);
        assertEquals((2500 + 5300 + 3000 + 4500 + 7000) / 5.0, stats.getAverage(), 1e-9);
    }

    @Test
    void analyzerConstructor_throwsOnNullList() {
        assertThrows(IllegalArgumentException.class, () -> new SpacexDataAnalyzer(null));
    }
}
