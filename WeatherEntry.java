import com.google.gson.annotations.Expose;

public class WeatherEntry {
    @Expose 
    private String id; // this is the station ID 
    @Expose    
    private String name;
    @Expose     
    private String state;    
    private String time_zone;    
    private double lat;    
    private double lon;    
    private String local_date_time;    
    private long local_date_time_full;    
    private double air_temp;    
    private double apparent_t;    
    private String cloud;    
    private double dewpt;    
    private double press;    
    private int rel_hum;    
    private String wind_dir;    
    private int wind_spd_kmh;    
    private int wind_spd_kt;
    
    private int sourceID = -1; // the ID of the content server that sent this data.

    public String getStationID() {
        return id;
    }

    public int getSourceID() {
        return sourceID;
    }

    public void addSourceID(int id) {
        this.sourceID = id;
    }
}
