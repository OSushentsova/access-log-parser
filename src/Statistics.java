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
            // Это реальный пользователь
            totalUserRequests++;
            uniqueUserIps.add(entry.getIp());
        }

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

    // НОВЫЙ МЕТОД: среднее количество ошибочных запросов в час
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

    // НОВЫЙ МЕТОД: средняя посещаемость одним пользователем (не ботом)
    public double getAverageVisitsPerUser() {
        if (uniqueUserIps.isEmpty() || totalUserRequests == 0) {
            return 0;
        }

        return (double) totalUserRequests / uniqueUserIps.size();
    }

    // Методы с использованием Stream API для статистики

    // Статистика ОС с сортировкой по убыванию доли
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

    // Статистика браузеров с сортировкой по убыванию доли
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

    // Фильтрация только реальных браузеров (исключая ботов)
    public Map<String, Integer> getRealBrowserCounts() {
        return browserCounts.entrySet().stream()
                .filter(entry -> !isBotBrowser(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }


    // Фильтрация только ботов
    public Map<String, Integer> getBotBrowserCounts() {
        return browserCounts.entrySet().stream()
                .filter(entry -> isBotBrowser(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    // Вспомогательный метод для определения бота по имени браузера
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


}