import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final HashSet<String> nonExistingPages;
    private int totalBotRequests;
    private int totalErrorRequests;
    private final HashSet<String> uniqueUserIps;
    private int totalUserRequests;

    private final Map<Integer, Integer> visitsPerSecond;
    private final HashSet<String> refererDomains;
    private final Map<String, Integer> visitsPerUser;

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
        this.nonExistingPages = new HashSet<>();

        this.totalBotRequests = 0;
        this.totalErrorRequests = 0;
        this.uniqueUserIps = new HashSet<>();
        this.totalUserRequests = 0;

        this.visitsPerSecond = new HashMap<>();
        this.refererDomains = new HashSet<>();
        this.visitsPerUser = new HashMap<>();
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

        int responseCode = entry.getResponseCode();
        String path = entry.getPath();

        if (entry.getResponseCode() == 200) {
            existingPages.add(path);
        } else if (responseCode == 404) {
            nonExistingPages.add(path);
        }

        if (responseCode >= 400 && responseCode < 600) {
            totalErrorRequests++;
        }

        String os = entry.getAgent().getOs();
        osCounts.put(os, osCounts.getOrDefault(os, 0) + 1);

        String browser = entry.getAgent().getBrowser();
        browserCounts.put(browser, browserCounts.getOrDefault(browser, 0) + 1);

        String userAgentRaw = entry.getAgent().getRawUserAgent();
        boolean isBot = userAgentRaw.toLowerCase().contains("bot");

        if (isBot) {
            totalBotRequests++;
        } else {
            totalUserRequests++;
            uniqueUserIps.add(entry.getIp());
            String ip = entry.getIp();
            visitsPerUser.put(ip, visitsPerUser.getOrDefault(ip, 0) + 1);

            int secondKey = entryTime.getSecond();
            visitsPerSecond.put(secondKey, visitsPerSecond.getOrDefault(secondKey, 0) + 1);
        }

        String referer = entry.getReferer();
        if (referer != null && !referer.isEmpty() && !referer.equals("-")) {
            String domain = extractDomain(referer);
            if (domain != null && !domain.isEmpty()) {
                refererDomains.add(domain);
            }
        }

        if (browser.equals("Googlebot")) {
            googlebotCount++;
        } else if (browser.equals("YandexBot")) {
            yandexbotCount++;
        }
    }

    private String extractDomain(String url) {
        try {
            String domain = url.toLowerCase();
            if (domain.startsWith("http://")) {
                domain = domain.substring(7);
            } else if (domain.startsWith("https://")) {
                domain = domain.substring(8);
            }

            int slashIndex = domain.indexOf('/');
            if (slashIndex > 0) {
                domain = domain.substring(0, slashIndex);
            }

            int portIndex = domain.indexOf(':');
            if (portIndex > 0) {
                domain = domain.substring(0, portIndex);
            }

            if (domain.startsWith("www.")) {
                domain = domain.substring(4);
            }

            return domain;
        } catch (Exception e) {
            return null;
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

    public int getPeakVisitsPerSecond() {
        return visitsPerSecond.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);


    }

    public HashSet<String> getRefererDomains() {
        return new HashSet<>(refererDomains);
    }

    public int getMaxVisitsPerUser() {
        return visitsPerUser.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }

    public Map<String, Integer> getVisitsPerUser() {
        return new HashMap<>(visitsPerUser);
    }

    public Map<Integer, Integer> getVisitsPerSecond() {
        return new HashMap<>(visitsPerSecond);
    }

    public Map<String, Double> getOsStatisticsSorted() {
        return getOsStatistics().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    public Map<String, Double> getBrowserStatisticsSorted() {
        return getBrowserStatistics().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        HashMap::new
                ));
    }

    public Map<String, Integer> getRealBrowserCounts() {
        return browserCounts.entrySet().stream()
                .filter(entry -> !isBotBrowser(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public Map<String, Integer> getBotBrowserCounts() {
        return browserCounts.entrySet().stream()
                .filter(entry -> isBotBrowser(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private boolean isBotBrowser(String browser) {
        return browser.equals("Googlebot") ||
                browser.equals("YandexBot") ||
                browser.equals("SemrushBot") ||
                browser.equals("MegaIndex") ||
                browser.equals("FeedFetcher") ||
                browser.contains("Bot");
    }

    public HashSet<String> getExistingPages() {
        return new HashSet<>(existingPages);
    }
    public HashSet<String> getNonExistingPages() {
        return new HashSet<>(nonExistingPages);
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

    public HashMap<String, Double> getBrowserStatistics() {
        HashMap<String, Double> browserShares = new HashMap<>();

        if (totalEntries == 0) {
            return browserShares;
        }

        for (Map.Entry<String, Integer> entry : browserCounts.entrySet()) {
            double share = (double) entry.getValue() / totalEntries;
            browserShares.put(entry.getKey(), share);
        }

        return browserShares;
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
    public int getTotalBotRequests() { return totalBotRequests; }
    public int getTotalErrorRequests() { return totalErrorRequests; }
    public int getUniqueUserIpsCount() { return uniqueUserIps.size(); }
    public int getTotalUserRequests() { return totalUserRequests; }

    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || totalUserRequests == 0) {
            return 0;
        }

        Duration duration = Duration.between(minTime, maxTime);
        long hours = duration.toHours();

        if (hours == 0) {
            return totalUserRequests;
        }

        return (double) totalUserRequests / hours;
    }

    public double getAverageErrorsPerHour() {
        if (minTime == null || maxTime == null || totalErrorRequests == 0) {
            return 0;
        }

        Duration duration = Duration.between(minTime, maxTime);
        long hours = duration.toHours();

        if (hours == 0) {
            return totalErrorRequests;
        }

        return (double) totalErrorRequests / hours;
    }

    public double getAverageVisitsPerUser() {
        if (uniqueUserIps.isEmpty() || totalUserRequests == 0) {
            return 0;
        }

        return (double) totalUserRequests / uniqueUserIps.size();
    }
}