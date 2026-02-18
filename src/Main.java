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

                System.out.println("Общее количество запросов (строк): " + totalRequests);
                System.out.println("\n=== СТАТИСТИКА ПО БОТАМ ===");
                System.out.println("Запросов от Googlebot: " + googlebotCount);
                System.out.println("Запросов от YandexBot: " + yandexbotCount);

                if (totalRequests > 0) {
                    double googleShare = (double) googlebotCount / totalRequests * 100;
                    double yandexShare = (double) yandexbotCount / totalRequests * 100;

                    System.out.printf("Доля Googlebot: %.2f%%\n", googleShare);
                    System.out.printf("Доля YandexBot: %.2f%%\n", yandexShare);
                }

                System.out.println("\n=== СТАТИСТИКА СТРАНИЦ ===");
                HashSet<String> existingPages = stats.getExistingPages();
                HashSet<String> nonExistingPages = stats.getNonExistingPages();

                System.out.println("Существующие страницы (код 200):");
                if (existingPages.isEmpty()) {
                    System.out.println("  Страницы с кодом 200 не найдены");
                } else {
                    System.out.println("  Всего уникальных страниц: " + existingPages.size());
                    // Выводим первые 5 для примера
                    int count = 0;
                    for (String page : existingPages) {
                        System.out.println("    " + page);
                        count++;
                        if (count >= 5) {
                            System.out.println("    ... и еще " + (existingPages.size() - 5));
                            break;
                        }
                    }
                }
                System.out.println("\nНесуществующие страницы (код 404):");
                if (nonExistingPages.isEmpty()) {
                    System.out.println("  Страницы с кодом 404 не найдены");
                } else {
                    System.out.println("  Всего уникальных страниц: " + nonExistingPages.size());
                    // Выводим первые 5 для примера
                    int count = 0;
                    for (String page : nonExistingPages) {
                        System.out.println("    " + page);
                        count++;
                        if (count >= 5) {
                            System.out.println("    ... и еще " + (nonExistingPages.size() - 5));
                            break;
                        }
                    }
                }

                System.out.println("\n=== СТАТИСТИКА ОПЕРАЦИОННЫХ СИСТЕМ (доли) ===");
                HashMap<String, Double> osShares = stats.getOsStatistics();
                if (osShares.isEmpty()) {
                    System.out.println("Нет данных по операционным системам");
                } else {
                    osShares.entrySet().stream()
                            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                            .forEach(entry -> {
                                System.out.printf("%s: %.4f (%.2f%%)\n",
                                        entry.getKey(),
                                        entry.getValue(),
                                        entry.getValue() * 100);
                            });
                }

                System.out.println("\n=== СТАТИСТИКА БРАУЗЕРОВ (доли) ===");
                HashMap<String, Double> browserShares = stats.getBrowserStatistics();
                if (browserShares.isEmpty()) {
                    System.out.println("Нет данных по браузерам");
                } else {
                    // Сортируем по убыванию доли
                    browserShares.entrySet().stream()
                            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                            .forEach(entry -> {
                                System.out.printf("%s: %.4f (%.2f%%)\n",
                                        entry.getKey(),
                                        entry.getValue(),
                                        entry.getValue() * 100);
                            });
                }

                System.out.println("\n=== СТАТИСТИКА ТРАФИКА ===");
                System.out.println("Общий трафик: " + stats.getTotalTraffic() + " байт");
                System.out.println("Первый запрос: " + stats.getMinTime());
                System.out.println("Последний запрос: " + stats.getMaxTime());
                System.out.printf("Средний трафик в час: %.2f байт/час\n", stats.getTrafficRate());

            } catch (LineTooLongException e) {
                System.out.println("Ошибка: " + e.getMessage());
                System.out.println("Анализ файла прерван.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}