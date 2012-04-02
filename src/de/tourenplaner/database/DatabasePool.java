package de.tourenplaner.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.tourenplaner.config.ConfigManager;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Map;
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
     *            A de.tourenplaner.database driver specific url with the form
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

        Map<?, ?> map = ConfigManager.getInstance().getEntryMap("c3p0", null);
        if (map != null) {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                if (key instanceof String && value instanceof String) {
                    System.setProperty("c3p0." + key, (String) value);
                } else {
                    log.warning("The de.tourenplaner.config file has an error within the value of the key \"c3p0\". " +
                            "The error is near the key " + key.toString());
                }
            }
        }

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

        return cpDataSource;
    }

}
