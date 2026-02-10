import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

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
        int count = 0;
        while (true) {
            System.out.println("Введите путь к файлу:");
            String path = new Scanner(System.in).nextLine();
            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();
            if (fileExists == false) {
                System.out.println("Указанный файл не существует");
            } else if (isDirectory == true) {
                System.out.println("Указанный путь является путём к папке, а не к файлу");
                continue;
            } else if (fileExists == true) {
                count++;
                System.out.println("Путь указан верно. Это файл номер " + count);

                try {
                    FileReader fileReader = new FileReader(path);
                    BufferedReader reader = new BufferedReader(fileReader);

                    String line;
                    int totalLines = 0;
                    int maxLength = 0;
                    int minLength = Integer.MAX_VALUE;

                    while ((line = reader.readLine()) != null) {
                        totalLines++;
                        int length = line.length();

                        if (length > 1024) {
                            throw new LineTooLongException(
                                    "Строка №" + totalLines + " содержит " + length +
                                            " символов, что превышает допустимый лимит 1024 символа."
                            );
                        }

                        if (length > maxLength) {
                            maxLength = length;
                        }

                        if (length < minLength) {
                            minLength = length;
                        }
                    }

                    if (totalLines == 0) {
                        minLength = 0;
                    }

                    reader.close();
                    fileReader.close();

                    System.out.println("Общее количество строк: " + totalLines);
                    System.out.println("Длина самой длинной строки: " + maxLength);
                    System.out.println("Длина самой короткой строки: " + minLength);

                } catch (LineTooLongException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
