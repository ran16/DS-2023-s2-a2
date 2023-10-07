import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Parser {
    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();

    // plain string ==> JSON string
    public String str2JSON(String str) {
        String[] lines = str.split("\n");

        String JSON_str = "{"; // add open bracket
        for (String line:lines) {
            // add , to the end of previous field, if it is not the first field
            if (JSON_str.equals("{")){
                JSON_str += "\n";
            } else {
                JSON_str += ",\n";
            }
            
            // Split line by ; to find key and value 
            String[] parts = line.split(":");
            
            // validate the string can be converted to JSON format
            if (parts.length < 2) {
                return "";
            }

            // Attach the key to the JSON string
            String key = parts[0].trim();
            JSON_str = JSON_str + " \"" + key + "\":";
            
            // Attach the value to the JSON string
            String value = parts[1].trim();
            for (int i=2; i<parts.length; i++) {    
                value = value + ":" +parts[i].trim(); // to caputre 15/04:00pm as a value
            }
            if (value.matches("-?\\d+(\\.\\d+)?")) { // if the value is a number, no need to add quotes
                JSON_str += value;
            } else {
                JSON_str = JSON_str + "\"" + value +"\"";
            }
        }

        JSON_str += "\n}"; // add closing bracket
        return JSON_str;
    }

    // JSON string ==> plain string
    public String JSON2String(String JSON_str) {
        String str = "";
        String[] parts = JSON_str.trim().split("\n");

        // Parse line by line
        for (String p : parts) {
            p = p.trim();
            if (p.matches("\".*"))
            {
                for (int i=0; i<p.length(); i++){ 
                    char c = p.charAt(i);
                    if (i == p.length()-1) { // Strip commas
                        if (c != ',') { 
                            str += c; 
                        }
                    } else if (c != '\"') { // Strip quotes
                        str += c;
                    }
                }
                str += "\n";
            } else if (p.matches("},")){ // add a line break for new entry
                str += "\n";
            }
        }
        return str;
    }



    // This function opens a txt file and reads line by line, concatenate the lines and return a big string.
    public String readFile(String FilePath) {
        String str = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                str = str + line + "\r\n";
            }
            return str;
        } catch (IOException e) {
            return null;
        }
    }

    // This functions takes a station ID and return the entry in JSON format
    public String GetEntrybyID(String FilePath, String station_ID) {
        
                
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath))) {
            String line;
            String result = "{\n";

            // parse the database file
            while ((line = reader.readLine()) != null) {
                // search for the id
                if (line.trim().equals("\"id\":\""+station_ID+"\",")) {
                    result = result + line + "\n"; 
                    break;
                }
            }
            
            // Can't find the station
            if (line == null) {
                return "";
            }

            // Concatenate the entry till reaching "}"
            while (!(line = reader.readLine()).trim().matches("}.*")) {
                result = result + line + "\n"; 
            }
            return result+"}\n";
        } catch (IOException e) {
            return null;
        }
    }

    // plain string in txt file ==> JSON string. Return empty string if fail to convert.
    public String txt2JSON(String FilePath) {
        String JSON_str = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath))) {
            String line = "";
            String entry = "";

            while ((line = reader.readLine()) != null) {
                // empty line means a new entry
                if (line.isEmpty() && !entry.isEmpty()) {
                    // convert the entry to JSON format and concatenate to JSON_str
                    String JSON_entry = str2JSON(entry);
                    JSON_str = JSON_str + JSON_entry + ",\n";
                    entry = ""; 
                } else {
                    // concatenate to entry
                    entry = entry + line + "\n";
                }
            }
            if(!entry.isEmpty()) {
                // convert the entry to JSON format and concatenate to JSON_str
                String JSON_entry = str2JSON(entry);
                JSON_str = JSON_str + JSON_entry;
            }

            JSON_str = "[\n" + JSON_str.trim() + "\n]";
            return JSON_str;
        } catch (IOException e) {
            return "";
        }
    }

    public int GetResponseCode(String response) {
        try {
            String[] parts = response.split(" ");
            return Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 400;
        }
    }

    // This function extract the body from PUT request
    public String extractBody(String request) {
        String body ="";

        boolean insideBlock = false;

        String[] lines = request.split("\n");

        for (String line:lines) {
            line = line.trim();

            if (line.equals("[")) {
                insideBlock = true;
            }

            if (insideBlock) {
                body = body + line+ "\n";
            }

            if (line.equals("]")) {
                insideBlock = false;
            }
        }

        return body;
    }

    // JSON string ==> WeatherEntry object
    public WeatherEntry[] JSON2Obj(String entry) {
        return gson.fromJson(entry, WeatherEntry[].class);
    }

    // WeatherEntry object ==> JSON string 
    public String Obj2JSON(WeatherEntry obj) {
        if (obj == null) {
            return "";
        } else {
            return gson.toJson(obj);
        }
    }

    public String dump(HashMap<String, WeatherEntry> database) {
        List<WeatherEntry> list = new ArrayList<>();
        
        // Create an iterator 
        Iterator<Map.Entry<String, WeatherEntry>> iterator = database.entrySet().iterator();

        // Iterate through the entries
        while (iterator.hasNext()) {
            Map.Entry<String, WeatherEntry> entry = iterator.next();
            WeatherEntry value = entry.getValue();
            // append to list
            list.add(value);
        }

        // Convert the list of weather data to JSON
        String result = "";
        try {
            result = gson.toJson(list);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return result;
    }
}
