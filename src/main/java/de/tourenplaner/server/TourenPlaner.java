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

package de.tourenplaner.server;


import de.tourenplaner.config.ConfigManager;
import de.tourenplaner.database.DatabaseManager;

import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class TourenPlaner {

    private static Logger log = Logger.getLogger("de.tourenplaner");


    /**
     * This is the main class of ToureNPlaner. It passes CLI parameters to the
     * handler and creates the httpserver
     */
    public static void main(String[] args) {
        String logFilename;
        CLIParser cliParser = new CLIParser(args);
        if (cliParser.getConfigFilePath() != null) {
            try {
                ConfigManager.init(new FileInputStream(cliParser.getConfigFilePath()));
            } catch (Exception e) {
                // ConfigManager either didn't like the path or the .config file at the path
                log.severe("Error reading configuration file from file: " + cliParser.getConfigFilePath() + '\n' +
                e.getMessage() + '\n' +
                "Using builtin configuration...");
            }
        } else {
            log.severe("Usage: \n\tjava -jar tourenplaner-server.jar -c \"config file\" " +
                       "[-f dump|text] [dumpgraph]\nDefaults are: builtin configuration, -f text");
        }
        ConfigManager cm = ConfigManager.getInstance();
        logFilename = cm.getEntryString("logfilepath", System.getProperty("user.home") + "/tourenplaner.log");

        // Add log file as loggind handler
        try{
            FileHandler fh = new FileHandler(logFilename, true);
            fh.setFormatter(new XMLFormatter());
            log.addHandler(fh);
            log.setLevel(Level.parse(cm.getEntryString("loglevel","info").toUpperCase()));
        } catch (IOException ex) {
            log.log(Level.WARNING,"Couldn't open log file "+logFilename, ex);
        }


        // initialize DatabaseManager
        if (cm.getEntryBool("private", false)) {
            try {
                DatabaseManager.initDatabaseManager(
                        cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"),
                        cm.getEntryString("dbuser", "tnpuser"),
                        cm.getEntryString("dbpw", "toureNPlaner"),
                        cm.getEntryString("dbdriverclass", "com.mysql.jdbc.Driver"));
                new DatabaseManager().getNumberOfUsers();
            } catch (SQLException e) {
                log.log(Level.SEVERE, "Couldn't establish database connection. DatabaseManager.getNumberOfUsers() " +
                        "was called to test and initialize database pool, but an SQLException was thrown", e);
                System.exit(1);
            } catch (PropertyVetoException e) {
                log.log(Level.SEVERE, "Couldn't establish database connection: " +
                        "could not set driver class of the database. The driver class is maybe wrong " +
                        "or the driver is maybe not accessible (not installed or not in class path).", e);
                System.exit(1);
            }
        }

        new HttpServer(cm);
    }
}
