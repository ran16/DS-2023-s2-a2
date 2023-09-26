import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TxtToJsonConverter {
    public static void main(String[] args) {
        // Replace this with your actual input data (e.g., read from a file or a string)
        String inputTxt = "id:IDS60901\nname:Adelaide (West Terrace /  ngayirdapira)\nstate: SA\n"
                + "time_zone:CST\nlat:-34.9\nlon:138.6\nlocal_date_time:15/04:00pm\nlocal_date_time_full:20230715160000\n"
                + "air_temp:13.3\napparent_t:9.5\ncloud:Partly cloudy\ndewpt:5.7\npress:1023.9\nrel_hum:60\nwind_dir:S\n"
                + "wind_spd_kmh:15\nwind_spd_kt:8\n\n"
                + "id:IDS60902\nname:Baggs end\nstate: Shire\ntime_zone:CST\nlat:-34.9\nlon:138.6\n"
                + "local_date_time:15/04:00pm\nlocal_date_time_full:20230715160000\nair_temp:13.3\napparent_t:9.5\n"
                + "cloud:Partly cloudy\ndewpt:5.7\npress:1023.9\nrel_hum:60\nwind_dir:S\nwind_spd_kmh:15\nwind_spd_kt:8\n";

        // Convert the input text to JSON
        String jsonOutput = convertTxtToJson(inputTxt);
        System.out.println(jsonOutput);
    }

    public static String convertTxtToJson(String inputTxt) {
        // Split the input text into individual records
        String[] records = inputTxt.split("\n\n");

        // Create a Gson instance with custom settings
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Create a StringBuilder to accumulate JSON output
        StringBuilder jsonOutput = new StringBuilder();

        for (String record : records) {
            // Split each record into key-value pairs
            String[] keyValuePairs = record.split("\n");

            // Create a WeatherData object and populate its fields
            WeatherData weatherData = new WeatherData();
            for (String keyValuePair : keyValuePairs) {
                String[] parts = keyValuePair.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    setFieldValue(weatherData, key, value);
                }
            }

            // Convert the WeatherData object to JSON and append it to the output
            String jsonRecord = gson.toJson(weatherData);
            jsonOutput.append(jsonRecord).append("\n");
        }

        return jsonOutput.toString();
    }

    private static void setFieldValue(WeatherData weatherData, String key, String value) {
        
    }
}