import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    private final Map<String, Integer> osCounts;
    private final Map<String, Integer> browserCounts;
    private int totalEntries;

    private int googlebotCount;
    private int yandexbotCount;

     private final HashSet<String> existingPages;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.osCounts = new HashMap<>();
        this.browserCounts = new HashMap<>();
        this.existingPages = new HashSet<>();
        this.totalEntries = 0;
        this.googlebotCount = 0;
        this.yandexbotCount = 0;
    }

    public void addEntry(LogEntry entry) {
        totalEntries++;
        totalTraffic += entry.getResponseSize();

        LocalDateTime entryTime = entry.getDateTime();

        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }

        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        }

        String os = entry.getAgent().getOs();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);

        String browser = entry.getAgent().getBrowser();
        browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);

        if (browser.equals("Googlebot")) {
            googlebotCount++;
        } else if (browser.equals("YandexBot")) {
            yandexbotCount++;
        }
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null) {
            return 0;
        }

        Duration duration = Duration.between(minTime, maxTime);
        long hours = duration.toHours();

        if (hours == 0) {
            return totalTraffic;
        }

        return (double) totalTraffic / hours;
    }

    public HashSet<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }

    public HashMap<String, Double> getOsStatistics() {
        HashMap<String, Double> osShares = new HashMap<>();

        if (totalEntries == 0) {
            return osShares;
        }

        for (Map.Entry<String, Integer> entry : osCounts.entrySet()) {
            double share = (double) entry.getValue() / totalEntries;
            osShares.put(entry.getKey(), share);
        }

        return osShares;
    }

    public Map<String, Integer> getBrowserCounts() {
        return new HashMap<>(browserCounts);
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getTotalTraffic() { return totalTraffic; }
    public LocalDateTime getMinTime() { return minTime; }
    public LocalDateTime getMaxTime() { return maxTime; }

}
