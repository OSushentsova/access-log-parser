import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        filePath();
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
            } else if (isDirectory == true){
                System.out.println("Указанный путь является путём к папке, а не к файлу");
                continue;
            } else if (fileExists == true){
                count++;
                System.out.println("Путь указан верно. Это файл номер " + count);
            }
        }
    }
}
