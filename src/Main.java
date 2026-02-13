import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

                System.out.println("\n=== СТАТИСТИКА ПО ОПЕРАЦИОННЫМ СИСТЕМАМ ===");
                Map<String, Integer> osStats = stats.getOsCounts();
                for (Map.Entry<String, Integer> entry : osStats.entrySet()) {
                    double percentage = (double) entry.getValue() / stats.getTotalEntries() * 100;
                    System.out.printf("%s: %d (%.2f%%)\n", entry.getKey(), entry.getValue(), percentage);
                }

                System.out.println("\n=== СТАТИСТИКА ПО БРАУЗЕРАМ ===");
                Map<String, Integer> browserStats = stats.getBrowserCounts();
                for (Map.Entry<String, Integer> entry : browserStats.entrySet()) {
                    double percentage = (double) entry.getValue() / stats.getTotalEntries() * 100;
                    System.out.printf("%s: %d (%.2f%%)\n", entry.getKey(), entry.getValue(), percentage);
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