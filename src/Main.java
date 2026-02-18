import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        filePath();
    }

    static class LineTooLongException extends RuntimeException {
        public LineTooLongException(String message) {
            super(message);
        }
    }

    public static void filePath() {
        int fileCounter = 0;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = scanner.nextLine();
            File file = new File(path);

            if (!file.exists()) {
                System.out.println("Указанный файл не существует");
                continue;
            }

            if (file.isDirectory()) {
                System.out.println("Указанный путь является путём к папке, а не к файлу");
                continue;
            }

            fileCounter++;
            System.out.println("Путь указан верно. Это файл номер " + fileCounter);

            Statistics stats = new Statistics();
            int totalRequests = 0;
            int parseErrors = 0;
            int googlebotCount = 0;
            int yandexbotCount = 0;

            try (FileReader fileReader = new FileReader(path);
                 BufferedReader reader = new BufferedReader(fileReader)) {

                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    int length = line.length();

                    if (length > 1024) {
                        throw new LineTooLongException(
                                "Строка №" + lineNumber + " содержит " + length +
                                        " символов, что превышает допустимый лимит 1024 символа."
                        );
                    }

                    try {
                        LogEntry entry = new LogEntry(line);
                        totalRequests++;

                        stats.addEntry(entry);

                        UserAgent ua = entry.getAgent();
                        String browser = ua.getBrowser();

                        if (browser.equals("Googlebot")) {
                            googlebotCount++;
                        } else if (browser.equals("YandexBot")) {
                            yandexbotCount++;
                        }
                    } catch (Exception e) {
                        System.out.println("Ошибка при парсинге строки " + lineNumber + ": " + e.getMessage());
                    }
                }

                System.out.println("\n=== ОБЩАЯ СТАТИСТИКА ===");
                System.out.println("Всего строк в файле: " + totalRequests);
                System.out.println("Успешно обработано: " + stats.getTotalEntries());
                System.out.println("Ошибок парсинга: " + parseErrors);

                System.out.println("\n=== СТАТИСТИКА ПО ПОЛЬЗОВАТЕЛЯМ ===");
                System.out.println("Запросов от реальных пользователей: " + stats.getTotalUserRequests());
                System.out.println("Уникальных пользователей (по IP): " + stats.getUniqueUserIpsCount());
                System.out.printf("\nМаксимальная посещаемость одним пользователем: %d\n",
                        stats.getMaxVisitsPerUser());

                System.out.println("\n=== СТАТИСТИКА СТРАНИЦ ===");
                System.out.println("Существующие страницы (код 200): " + stats.getExistingPages().size());
                System.out.println("Несуществующие страницы (код 404): " + stats.getNonExistingPages().size());

                System.out.println("\n=== СПИСОК САЙТОВ, С КОТОРЫХ ЕСТЬ ССЫЛКИ ===");
                HashSet<String> refererDomains = stats.getRefererDomains();
                if (refererDomains.isEmpty()) {
                    System.out.println("Нет данных о referer-ах");
                } else {
                    System.out.println("Всего уникальных доменов: " + refererDomains.size());
                    // Выводим первые 10 для примера
                    int count = 0;
                    for (String domain : refererDomains) {
                        System.out.println("  " + domain);
                        count++;
                        if (count >= 10) {
                            System.out.println("  ... и еще " + (refererDomains.size() - 10));
                            break;
                        }
                    }
                }

                System.out.println("\n=== СТАТИСТИКА ОШИБОК ===");
                System.out.println("Запросов с ошибками (4xx, 5xx): " + stats.getTotalErrorRequests());

                System.out.println("\n=== СРЕДНИЕ ЗНАЧЕНИЯ ===");
                System.out.println("Общий трафик: " + stats.getTotalTraffic() + " байт");
                System.out.printf("Средний трафик в час: %.2f байт/час\n", stats.getTrafficRate());
                System.out.printf("Среднее количество посещений пользователей в час: %.2f\n",
                        stats.getAverageVisitsPerHour());
                System.out.printf("Среднее количество ошибок в час: %.2f\n",
                        stats.getAverageErrorsPerHour());
                System.out.printf("Средняя посещаемость одним пользователем: %.2f\n",
                        stats.getAverageVisitsPerUser());
                System.out.println("Первый запрос: " + stats.getMinTime());
                System.out.println("Последний запрос: " + stats.getMaxTime());
                System.out.printf("Пиковая посещаемость сайта (в секунду): %d\n",
                        stats.getPeakVisitsPerSecond());

                System.out.println("\n=== СТАТИСТИКА ОПЕРАЦИОННЫХ СИСТЕМ (сортировка Stream API) ===");
                Map<String, Double> osStats = stats.getOsStatisticsSorted();
                for (Map.Entry<String, Double> entry : osStats.entrySet()) {
                    System.out.printf("%s: %.4f (%.2f%%)\n",
                            entry.getKey(), entry.getValue(), entry.getValue() * 100);
                }

                System.out.println("\n=== СТАТИСТИКА БРАУЗЕРОВ (сортировка Stream API) ===");
                Map<String, Double> browserStats = stats.getBrowserStatisticsSorted();
                for (Map.Entry<String, Double> entry : browserStats.entrySet()) {
                    System.out.printf("%s: %.4f (%.2f%%)\n",
                            entry.getKey(), entry.getValue(), entry.getValue() * 100);
                }

                System.out.println("\n=== СТАТИСТИКА ПО БОТАМ ===");
                System.out.println("Всего запросов от ботов: " + stats.getTotalBotRequests());
                System.out.println("Из них Googlebot: " + stats.getBrowserCounts().getOrDefault("Googlebot", 0));
                System.out.println("Из них YandexBot: " + stats.getBrowserCounts().getOrDefault("YandexBot", 0));

                System.out.println("\n=== ТОЛЬКО РЕАЛЬНЫЕ БРАУЗЕРЫ (без ботов) ===");
                Map<String, Integer> realBrowsers = stats.getRealBrowserCounts();
                int realTotal = realBrowsers.values().stream().mapToInt(Integer::intValue).sum();
                for (Map.Entry<String, Integer> entry : realBrowsers.entrySet()) {
                    double percentage = (double) entry.getValue() / realTotal * 100;
                    System.out.printf("%s: %d (%.2f%% от реальных)\n",
                            entry.getKey(), entry.getValue(), percentage);
                }

            } catch (LineTooLongException e) {
                System.out.println("Ошибка: " + e.getMessage());
                System.out.println("Анализ файла прерван.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}