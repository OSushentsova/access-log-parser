import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LogEntry {
    private final String ipAddr;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent agent;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

    public LogEntry(String logLine) {
        try {
            String[] parts = logLine.split(" ", 2);
            this.ipAddr = parts[0].trim();

            String remaining = parts[1];

            int dateStart = remaining.indexOf('[');
            int dateEnd = remaining.indexOf(']');
            String dateStr = remaining.substring(dateStart + 1, dateEnd);
            this.time = LocalDateTime.parse(dateStr, DATE_FORMATTER);

            remaining = remaining.substring(dateEnd + 1).trim();

            int quoteStart = remaining.indexOf('"');
            int quoteEnd = remaining.indexOf('"', quoteStart + 1);
            String requestLine = remaining.substring(quoteStart + 1, quoteEnd);
            String[] requestParts = requestLine.split(" ");

            if (requestParts.length >= 2) {
                this.method = HttpMethod.fromString(requestParts[0]);
                this.path = requestParts[1];
            } else {
                this.method = HttpMethod.UNKNOWN;
                this.path = "";
            }

            remaining = remaining.substring(quoteEnd + 1).trim();

            String[] responseParts = remaining.split(" ", 3);
            this.responseCode = Integer.parseInt(responseParts[0]);
            this.responseSize = parseSize(responseParts[1]);

            remaining = responseParts.length > 2 ? responseParts[2] : "";

            if (remaining.startsWith("\"")) {
                int refStart = remaining.indexOf('"');
                int refEnd = remaining.indexOf('"', refStart + 1);
                this.referer = remaining.substring(refStart + 1, refEnd);
                remaining = remaining.substring(refEnd + 1).trim();
            } else {
                this.referer = "";
            }

            if (remaining.startsWith("\"")) {
                int uaStart = remaining.indexOf('"');
                int uaEnd = remaining.lastIndexOf('"');
                String uaString = remaining.substring(uaStart + 1, uaEnd);
                this.agent = new UserAgent(uaString);
            } else {
                this.agent = new UserAgent("");
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось распарсить строку лога: " + logLine, e);
        }
    }

    private int parseSize(String sizeStr) {
        if (sizeStr.equals("-")) {
            return 0;
        }
        try {
            return Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public LocalDateTime getDateTime() { return time; }
    public String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public int getResponseSize() { return responseSize; }
    public String getReferer() { return referer; }
    public UserAgent getAgent() { return agent; }
}
