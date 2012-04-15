package de.tourenplaner.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.tourenplaner.config.ConfigManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class DatabasePool {

    private static Logger log = Logger.getLogger("de.tourenplaner.database");

    /**
     * This method will create and return a DataSource object.
     * The DataSource object has an underlying database pool,
     * so you can get from it pooled connections.
     *
     * @param url
     *            A database driver specific url with the form
     *            <i>jdbc:subprotocol:subname</i> where <i>subname</i> is the server address with
     *            the database name (for example &quot;tourenplaner&quot;)
     *            and at the end some connection properties if needed
     *            (for example &quot;?autoReconnect=true&quot;)
     *            </br> Example:
     *            "jdbc:mysql://localhost:3306/tourenplaner?autoReconnect=true"
     * @param userName
     *            User name of the database user account
     * @param password
     *            To the user corresponding password
     * @param driverClass
     *            The driver class, for example: "com.mysql.jdbc.Driver"
     * @return The corresponding DataSource object, but not null.
     *
     * @see java.sql.DriverManager#getConnection(java.lang.String,java.lang.String, java.lang.String)
     * @throws PropertyVetoException Thrown if driverClass String for the database is not correct
     *         or if the driver is not accessible.
     */
    public static DataSource createDataSource(String url, String userName, String password, String driverClass)
            throws PropertyVetoException {

        ComboPooledDataSource cpDataSource = new ComboPooledDataSource();

        try {
            cpDataSource.setDriverClass(driverClass);
        } catch (PropertyVetoException e) {
            log.severe("Cannot set driver class '" + driverClass + "' for the database pool, " +
                    "thereby it is not possible to create the database pool.");
            throw e;
        }
        cpDataSource.setJdbcUrl(url);
        cpDataSource.setUser(userName);
        cpDataSource.setPassword(password);

        /*
            Some config options and their default values for the database pool c3p0
            See http://www.mchange.com/projects/c3p0/#configuration_properties for more details and options

            "acquireIncrement" : 3,
            "initialPoolSize" : 3,
            "maxPoolSize" : 15,
            "minPoolSize" : 3,

            "maxConnectionAge" : 0,
            "maxIdleTime" : 0,
            "maxIdleTimeExcessConnections" : 0,

            "maxStatements" : 0,
            "maxStatementsPerConnection" : 0,

            "acquireRetryAttempts" : 30,
            "acquireRetryDelay" : 1000,
            "breakAfterAcquireFailure" : false
         */

        ConfigManager configManager = ConfigManager.getInstance().getEntryMap("databasepool", null);
        cpDataSource.setInitialPoolSize(configManager.getEntryInt("initialPoolSize", 3));
        cpDataSource.setMaxPoolSize(configManager.getEntryInt("maxPoolSize", 15));

        return cpDataSource;
    }

}
