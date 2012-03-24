/**
 * 
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
 * @author Sascha Meusel
 * 
 */
public class DatabaseManagerTest {

	static DatabaseManager dbm = null;
	static String dbmFailureMessage = null;
	static int testUserID = -1;
	static String userFailureMessage = null;

	/**
	 * Prepares test run.
	 */
	@BeforeClass
	public final static void prepareTestRun() {
		final ConfigManager cm = ConfigManager.getInstance();
		try {
            dbm = DatabasePool.getDatabaseManager(
                    cm.getEntryString("dburi", "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"),
                    cm.getEntryString("dbuser", "tnpuser"),
                    cm.getEntryString("dbpw", "toureNPlaner"),
                    cm.getEntryString("dbdriverclass", "com.mysql.jdbc.Driver"));
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
	public final static void cleanUpAfterTestRun() {
		if (dbm != null) {
			try {
				dbm.deleteRequestsOfUser(testUserID);
				dbm.deleteUser("1337testuser@tourenplaner");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#DatabaseManager(javax.sql.DataSource)}
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
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("Not yet implemented"); // TODO
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
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#addNewVerifiedUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)}
	 * .
	 */
	@Test
	public final void testAddNewVerifiedUser() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#updateRequest(de.tourenplaner.database.RequestDataset)}.
	 */
	@Test
	public final void testUpdateRequest() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#updateUser(de.tourenplaner.database.UserDataset)}.
	 */
	@Test
	public final void testUpdateUser() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#deleteRequest(int)}.
	 */
	@Test
	public final void testDeleteRequest() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#deleteRequestsOfUser(int)}.
	 */
	@Test
	public final void testDeleteRequestsOfUser() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#deleteUser(int)}.
	 */
	@Test
	public final void testDeleteUserInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#deleteUser(java.lang.String)}.
	 */
	@Test
	public final void testDeleteUserString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#getAllRequests(int, int)}
	 * .
	 */
	@Test
	public final void testGetAllRequestsIntInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#getRequest(int)}.
	 */
	@Test
	public final void testGetRequest() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#getRequests(int, int, int)}.
	 */
	@Test
	public final void testGetRequestsIntIntInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#getAllUsers()}.
	 */
	@Test
	public final void testGetAllUsers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#getAllUsers(int, int)}.
	 */
	@Test
	public final void testGetAllUsersIntInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for
	 * {@link de.tourenplaner.database.DatabaseManager#getUser(java.lang.String)}.
	 */
	@Test
	public final void testGetUserString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link de.tourenplaner.database.DatabaseManager#getUser(int)}.
	 */
	@Test
	public final void testGetUserInt() {
		fail("Not yet implemented"); // TODO
	}

}
