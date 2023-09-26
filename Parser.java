import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Parser {

    // This function convert a string to JSON format.
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

    // This functions takes in a JSON str (simple ones without nesting brackets, and with \n at the end of each entry), and returns a plain string. 
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
        // strip brackets
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

    // This function opens a txt file and convert its content to JSON format, providing it's valid. otherwise return empty string.
    public String txt2JSON(String FilePath) {
        String JSON_str = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath))) {
            String line = "";
            String entry = "";

            System.out.println(line);

            // String entry = line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
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
            System.out.println("txt2JSON parsed: \n"+JSON_str);
            return JSON_str;
        } catch (IOException e) {
            return "";
        }
    }

    public int GetResponseCode(String response) {
        String[] parts = response.split(" ");
        return Integer.parseInt(parts[1]);
    }
}
