import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

    // This function reads a txt file and convert its content to JSON format.
    public String txt2JSON(String FilePath) {
        String JSON_str = "{";
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // add , to the end of previous field, if it is not the first field
                if (JSON_str.equals("{")){
                    JSON_str += "\n";
                } else {
                    JSON_str += ",\n";
                }
                // Split line by ; to find key and value 
                String[] parts = line.split(":");

                // Attach the key to the JSON string
                String key = parts[0].trim();
                JSON_str = JSON_str + " \"" + key + "\":";

                // Attach the value to the JSON string
                String value = parts[1];
                for (int i=2; i<parts.length; i++) {    
                    value = value + ":" +parts[i].trim(); // to caputre 15/04:00pm as a value
                }
                if (value.matches("-?\\d+(\\.\\d+)?")) { // if the value is a number, no need to add quotes
                    JSON_str += value;
                } else {
                    JSON_str = JSON_str + "\"" + value +"\"";
                }
            }
            JSON_str += "\n}\n";
            return JSON_str;
        } catch (IOException e) {
            return "";
        }
    }

    public String JSON2String(String JSON_str) {
        String str = "";
        String[] parts = str.split(",\n");

        for (String p : parts) {
            if (!p.equals("{") && !p.equals("}")) {
                String sub_str = "";
                String[] sections = p.split("\"");
                for (int i=0; i<sections.length; i++) {
                    if (i==sections.length-1 && sections[i].equals(",")) {
                        break;
                    }
                    sub_str = sub_str+ sections[i];
                }

            }
        }

    }
}
