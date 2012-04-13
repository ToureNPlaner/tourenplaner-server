package de.tourenplaner.utils;

import de.tourenplaner.server.ErrorMessage;

import java.util.*;

/**
 * Use this class to generate easily a table with all ErrorMessages for AsciiDoc
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ErrorMessagesToAsciiDoc {

    public static void main(String[] args) {

        HashMap<String, List<String>> table = new HashMap<String, List<String>>();
        
        for (ErrorMessage eMessage : ErrorMessage.values()) {
            if (!table.containsKey(eMessage.errorId) ) {
                table.put(eMessage.errorId, new ArrayList<String>());
                table.get(eMessage.errorId).add(eMessage.status.toString());
                table.get(eMessage.errorId).add(eMessage.message);
            }
            if (!"".equals(eMessage.details)) {
                table.get(eMessage.errorId).add(eMessage.details);
            }
        }

        String[] keyArray = table.keySet().toArray(new String[table.keySet().size()]);
        Arrays.sort(keyArray);

        //printing attributes
        for (String errorId : keyArray) {
            String errorIdLowerCase = errorId.toLowerCase();
            String idAttribute = errorIdLowerCase + "_id";
            String statusAttribute = errorIdLowerCase + "_status";
            String messageAttribute = errorIdLowerCase + "_message";
            String descriptionAttribute = errorIdLowerCase + "_description";
            
            System.out.println(":" + idAttribute + ": " + errorId);
            System.out.println(":" + statusAttribute + ": " + table.get(errorId).get(0));
            System.out.println(":" + messageAttribute + ": " + table.get(errorId).get(1));
            System.out.println(":" + descriptionAttribute + ": ");
            
            System.out.println(":" + errorIdLowerCase + ": " +
                    "<<{" + idAttribute + "},{" + idAttribute + "}>>" +
                    " - {" + statusAttribute + "}" +
                    " - {" + messageAttribute + "}");
            System.out.println();
        }


        System.out.println();
        System.out.println();
        System.out.println("== Errors");
        
        System.out.println("[options=\"header,autowidth\",cols=\"4*\"]");
        System.out.println("|==================================");
        System.out.println("|Error ID |Http Statuscode |Message and possible Details |Description");

        
        
        for (String errorId : keyArray) {

            String errorIdLowerCase = errorId.toLowerCase();
            String idAttribute = "{" + errorIdLowerCase + "_id}";
            String statusAttribute = "{" + errorIdLowerCase + "_status}";
            String messageAttribute = "{" + errorIdLowerCase + "_message}";
            String descriptionAttribute = "{" + errorIdLowerCase + "_description}";
            
            System.out.print("|" + idAttribute + "[[" + idAttribute+ "]] |" + statusAttribute + "|" + messageAttribute);

            for (int i=2; i < table.get(errorId).size(); i++) {
                System.out.println(" +");
                System.out.print("- " + table.get(errorId).get(i));
            }
            if (table.get(errorId).size() > 2) {
                System.out.println();
            } else {
                System.out.print(" ");
            }

            System.out.println("|" + descriptionAttribute);
            
        }


        System.out.println("|==================================");

    }

}
