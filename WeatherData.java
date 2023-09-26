import com.google.gson.annotations.SerializedName;

public class WeatherData {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("state")
    private String state;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    @SerializedName("local_date_time")
    private String localDateTime;

    @SerializedName("local_date_time_full")
    private long localDateTimeFull;

    @SerializedName("air_temp")
    private double airTemperature;

    @SerializedName("apparent_t")
    private double apparentTemperature;

    @SerializedName("cloud")
    private String cloud;

    @SerializedName("dewpt")
    private double dewpoint;

    @SerializedName("press")
    private double pressure;

    @SerializedName("rel_hum")
    private int relativeHumidity;

    @SerializedName("wind_dir")
    private String windDirection;

    @SerializedName("wind_spd_kmh")
    private int windSpeedKMH;

    @SerializedName("wind_spd_kt")
    private int windSpeedKT;

    // Getters and setters (You can generate these using your IDE)
}
