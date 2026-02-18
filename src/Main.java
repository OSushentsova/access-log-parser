import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        filePath();
    }

    // Собственное исключение для слишком длинных строк
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

                    // Проверка на слишком длинную строку
                    if (length > 1024) {
                        throw new LineTooLongException(
                                "Строка №" + lineNumber + " содержит " + length +
                                        " символов, что превышает допустимый лимит 1024 символа."
                        );
                    }

                    totalRequests++;

                    String userAgent = extractUserAgent(line);
                    if (userAgent == null) {
                        continue;
                    }

                    String botName = extractBotName(userAgent);
                    if (botName == null) {
                        continue;
                    }

                    if (botName.equals("Googlebot")) {
                        googlebotCount++;
                    } else if (botName.equals("YandexBot")) {
                        yandexbotCount++;
                    }
                }

                System.out.println("Общее количество запросов (строк): " + totalRequests);
                System.out.println("Запросов от Googlebot: " + googlebotCount);
                System.out.println("Запросов от YandexBot: " + yandexbotCount);

                if (totalRequests > 0) {
                    double googleShare = (double) googlebotCount / totalRequests * 100;
                    double yandexShare = (double) yandexbotCount / totalRequests * 100;

                    System.out.printf("Доля Googlebot: %.2f%%\n", googleShare);
                    System.out.printf("Доля YandexBot: %.2f%%\n", yandexShare);
                }

            } catch (LineTooLongException e) {
                System.out.println("Ошибка: " + e.getMessage());
                System.out.println("Анализ файла прерван.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static String extractUserAgent(String line) {
        // Ищем открывающую скобку, внутри которой обычно содержится информация о боте
        int start = line.indexOf('(');
        int end = line.indexOf(')', start + 1);

        if (start == -1 || end == -1) {
            return null;
        }

        return line.substring(start + 1, end);
    }

    private static String extractBotName(String userAgentPart) {
        String[] parts = userAgentPart.split(";");

        if (parts.length < 2) {
            return null;
        }

        String fragment = parts[1].trim();

        int slashIndex = fragment.indexOf('/');
        if (slashIndex != -1) {
            fragment = fragment.substring(0, slashIndex);
        }

        return fragment.trim();
    }
}