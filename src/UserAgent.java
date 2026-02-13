import java.util.Locale;

public class UserAgent {

    private final String browser;
    private final String os;

    public UserAgent(String userAgentString) {

        this.browser = extractBrowser(userAgentString);
        this.os = extractOS(userAgentString);
    }

    private String extractBrowser(String ua) {
        String uaLower = ua.toLowerCase(Locale.ROOT);

        if (uaLower.contains("googlebot")) {
            return "Googlebot";
        } else if (uaLower.contains("yandexbot")) {
            return "YandexBot";
        }

        if (uaLower.contains("edg/") || uaLower.contains("edge/")) {
            return "Edge";
        } else if (uaLower.contains("opr/") || uaLower.contains("opera")) {
            return "Opera";
        } else if (uaLower.contains("chrome/") && !uaLower.contains("edg/") && !uaLower.contains("opr/")) {
            return "Chrome";
        } else if (uaLower.contains("firefox/")) {
            return "Firefox";
        } else if (uaLower.contains("safari/") && !uaLower.contains("chrome/")) {
            return "Safari";
        } else if (uaLower.isEmpty() || uaLower.equals("-")) {
            return "Unknown/Empty";
        } else {
            return "Other";
        }
    }

    private String extractOS(String ua) {
        String uaLower = ua.toLowerCase(Locale.ROOT);

        if (uaLower.contains("windows")) {
            return "Windows";
        } else if (uaLower.contains("mac os") || uaLower.contains("macos")) {
            return "macOS";
        } else if (uaLower.contains("linux")) {
            return "Linux";
        } else if (uaLower.contains("android")) {
            return "Android";
        } else if (uaLower.contains("iphone") || uaLower.contains("ipad") || uaLower.contains("ios")) {
            return "iOS";
        } else {
            return "Other";
        }
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }
}
