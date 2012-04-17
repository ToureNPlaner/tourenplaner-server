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

package de.tourenplaner.database;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tourenplaner.config.ConfigManager;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class DatabaseManagerTest {

    static boolean doTest = false;
    
	static DatabaseManager dbm = null;
	static String dbmFailureMessage = null;
	static int testUserID = -1;
	static String userFailureMessage = null;

	/**
	 * Prepares test run.
	 */
	@BeforeClass
	public static void prepareTestRun() {
        String configPath = DatabaseManagerTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                        "../data/test/database-junit.conf";

        try {
            ConfigManager.init(configPath);
        } catch (Exception e) {
            return;
        }

        final ConfigManager cm = ConfigManager.getInstance();
        doTest = cm.getEntryBool("private", false);

        System.out.println();
        if (doTest) {
            System.out.println("### Database JUnit test will not be executed. ###");

        } else {
            System.out.println("### Database JUnit test will not be executed. ###");

        }
        System.out.println();
        
        
		try {
            DatabaseManager.initDatabaseManager(
                    cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"),
                    cm.getEntryString("dbuser", "tnpuser"),
                    cm.getEntryString("dbpw", "toureNPlaner"),
                    cm.getEntryString("dbdriverclass", "com.mysql.jdbc.Driver"));
            dbm = new DatabaseManager();
		} catch (PropertyVetoException e) {
            dbmFailureMessage = "No Database Connection established. "
                    + "Connection parameter:\n"
                    + cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/")
                    + "\ndbname: "
                    + cm.getEntryString("dbname", "tourenplaner")
                    + "\ndbuser: " + cm.getEntryString("dbuser", "tnpuser")
                    + "\ndbpw: " + cm.getEntryString("dbpw", "toureNPlaner")
                    + "\n" + e.getMessage();
        }
        if (dbm != null) {
			try {
				UserDataset user = dbm.addNewUser("1337testuser@tourenplaner",
						"DmGT9B354DFasH673aGFBM3", "hmAhgAN68sdKNfdA9sd876k0",
						"John", "Doe", "Musterstraße 42", false);
				if (user != null) {
					testUserID = user.userid;
				} else {
					userFailureMessage = "TestUser existed before test run.";
				}
			} catch (SQLException e) {
				userFailureMessage = "\nSQL error while inserting TestUser."
						+ "\n" + e.getMessage();
			}

		}
		if ((dbm != null) && (dbmFailureMessage == null)) {
			System.out.println("Database successful connected.");
		} else {
			System.out.println(dbmFailureMessage);
		}
		if (userFailureMessage == null) {
			System.out.println("TestUser successful inserted.");
		} else {
			System.out.println(userFailureMessage);
		}

	}

	/**
	 * Cleaning up after test run.
	 */
	@AfterClass
	public static void cleanUpAfterTestRun() {
		if (dbm != null) {
			try {
				dbm.deleteUser(testUserID);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#DatabaseManager()}
	 * .
	 */
	@Test
	public final void testDatabaseManager() {
		assertTrue(dbmFailureMessage, (dbm != null)
				&& (dbmFailureMessage == null));
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#addNewRequest(int, String, byte[])}.
	 */
	@Test
	public final void testAddNewRequest() {
		try {
			int requestID = dbm.addNewRequest(testUserID,
					"testRequest", "jsonRequestTestBlob".getBytes());
			assertFalse("dbm is null", dbm == null);
			assertFalse("returned request id should never be <= 0", requestID <= 0);
		} catch (SQLFeatureNotSupportedException e) {
			fail(e.getLocalizedMessage());
		} catch (SQLException e) {
            fail(e.getLocalizedMessage());
		}

	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#addNewUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)}
	 * .
	 */
	@Test
	public final void testAddNewUser() {
		assertTrue(userFailureMessage, userFailureMessage == null);
	}

}
