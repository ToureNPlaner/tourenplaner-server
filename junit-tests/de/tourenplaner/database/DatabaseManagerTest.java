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

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tourenplaner.config.ConfigManager;

import static org.junit.Assert.*;

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
            System.out.println("### Database JUnit test will be executed. ###");
            System.out.println();
            System.out.println("If the test should not be executed, set the value 'private' to false within ");
            System.out.println("the config file " + configPath);
            System.out.println();
        } else {
            System.out.println("### Database JUnit test will _NOT_ be executed. ###");
            System.out.println();
            System.out.println("If the test should be executed, set the value 'private' to true within ");
            System.out.println("the config file " + configPath);
            System.out.println();
            return;
        }
        
        
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
        if (!doTest) return;
		if (dbm != null) {
			try {
                UserDataset user = dbm.getUser("1337testuser@tourenplaner");
                if (user != null) {
                    dbm.deleteUser(user.userid);
                }
				dbm.deleteUser(testUserID);
			} catch (SQLException e) {
                fail(e.getMessage());
			}
		}
	}


	@Test
	public final void testDatabaseManager() {
        if (!doTest) return;
		assertTrue(dbmFailureMessage, (dbm != null)
				&& (dbmFailureMessage == null));
	}


	@Test
	public final void testRequest() {
        if (!doTest) return;
		try {
			int requestID = dbm.addNewRequest(testUserID,
					"testAlgorithm", "jsonRequestTestBlob".getBytes());
			assertFalse("dbm is null", dbm == null);
			assertFalse("returned request id should never be <= 0", requestID <= 0);
            
            RequestDataset req = dbm.getRequest(requestID);
            assertEquals("userid not correct", testUserID, req.userID);
            assertEquals("algorithm not correct", "testAlgorithm", req.algorithm);
            // JSONObjects will not get retrieved through getRequest
            assertEquals("request not correct", null, req.jsonRequest);
            
            // test if sql syntax errors occur
            dbm.updateRequest(req);
            dbm.updateRequestWithComputeResult(requestID, "some byte array".getBytes(), 10, 1337);
            dbm.updateRequestAsFailed(requestID, "an error message".getBytes());
            
            dbm.getAllRequests(10, 20);
            dbm.getJsonRequest(requestID);
            dbm.getJsonResponse(requestID);
            dbm.getRequests(testUserID, 10, 20);
            
            dbm.getNumberOfRequests();
            dbm.getNumberOfRequestsWithUserId(testUserID);
            
		} catch (SQLFeatureNotSupportedException e) {
			fail(e.getMessage());
		} catch (SQLException e) {
            fail(e.getMessage());
		}

	}

    
	@Test
	public final void testUser() {
        if (!doTest) return;
		assertTrue(userFailureMessage, userFailureMessage == null);
        try {
            UserDataset user = dbm.getUser(testUserID);

            assertEquals("email not correct", "1337testuser@tourenplaner", user.email);
            assertEquals("passwordhash not correct", "DmGT9B354DFasH673aGFBM3", user.passwordhash);
            assertEquals("salt not correct", "hmAhgAN68sdKNfdA9sd876k0", user.salt);
            assertEquals("firstName not correct", "John", user.firstName);
            assertEquals("lastName not correct", "Doe", user.lastName);
            assertEquals("address not correct", "Musterstraße 42", user.address);
            assertEquals("admin not correct", false, user.admin);

            // test if sql syntax errors occur
            dbm.getAllUsers();
            dbm.getAllUsers(10, 20);
            dbm.getNumberOfUsers();
            

        } catch (SQLException e) {
            fail(e.getMessage());
        }

    }

}
