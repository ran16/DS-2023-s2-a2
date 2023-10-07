import com.google.gson.annotations.Expose;

public class WeatherEntry {
    @Expose 
    private String id; // this is the station ID 
    @Expose    
    private String name;
    @Expose     
    private String state;
    @Expose    
    private String time_zone;
    @Expose    
    private double lat;    
    @Expose    
    private double lon;    
    @Expose    
    private String local_date_time;    
    @Expose    
    private long local_date_time_full;    
    @Expose    
    private double air_temp;    
    @Expose    
    private double apparent_t;    
    @Expose    
    private String cloud;    
    @Expose    
    private double dewpt;    
    @Expose    
    private double press;    
    @Expose    
    private int rel_hum;    
    @Expose    
    private String wind_dir;    
    @Expose    
    private int wind_spd_kmh;    
    @Expose    
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
