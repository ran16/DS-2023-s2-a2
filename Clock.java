import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This Clock library provides functionalities regarding Lamport Clock.
public class Clock {

    // This function extracts Lamport time from the response recieved. If failed, returns -1.
    public int GetRecievedTime(String response) {
        // Define the regular expression pattern to match "Lamport Time: <number>"
        String regex = "Lamport Time: (\\d+)";
        Pattern pattern = Pattern.compile(regex);

        // Use a Matcher to find the pattern in the input string
        Matcher matcher = pattern.matcher(response);

        // Check if the pattern was found
        if (matcher.find()) {
            // Extract the number from the matched group
            String numberStr = matcher.group(1);

            // Parse the number as an integer and return it
            try {
                return Integer.parseInt(numberStr);
            } catch (NumberFormatException e) {
                System.out.println("Time Sync failed. Invalid Lamport Time format: " + numberStr);
                return -1;
            }
        } else {
            return -1;
        }
    }
}