/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
            
            System.out.println(":" + idAttribute + ": " + errorId);
            System.out.println(":" + statusAttribute + ": " + table.get(errorId).get(0));
            System.out.println(":" + messageAttribute + ": " + table.get(errorId).get(1));
            
            System.out.println(":" + errorIdLowerCase + ": " +
                    "<<{" + idAttribute + "},{" + idAttribute + "}>>" +
                    " - {" + statusAttribute + "}" +
                    " - {" + messageAttribute + "}");
            System.out.println();
        }


        System.out.println();
        System.out.println();
        System.out.println("== Errors");
        
        System.out.println("[options=\"header,autowidth\",cols=\"3*\"]");
        System.out.println("|==================================");
        System.out.println("|Error ID |Http Statuscode |Message and possible Details");

        
        
        for (String errorId : keyArray) {

            String errorIdLowerCase = errorId.toLowerCase();
            String idAttribute = "{" + errorIdLowerCase + "_id}";
            String statusAttribute = "{" + errorIdLowerCase + "_status}";
            String messageAttribute = "{" + errorIdLowerCase + "_message}";
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
            System.out.println();
        }


        System.out.println("|==================================");

    }

}
