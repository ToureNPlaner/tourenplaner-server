package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Sascha Meusel
 *
 */
public class SqlStatementString {
    
    private String sqlStatementString;
    private int generateKeysParameter;

    /**
     * Constructs an object which bundles the sql string and the generate-keys parameter together.
     * @param sqlStatementString The SQL String
     * @param generateKeysParameter Should be either java.sql.Statement.NO_GENERATED_KEYS or
     *                            java.sql.Statement.RETURN_GENERATED_KEYS
     */
    public SqlStatementString(String sqlStatementString, int generateKeysParameter) {
        this.sqlStatementString = sqlStatementString;
        this.generateKeysParameter = generateKeysParameter;
    }

    /**
     * Constructs an object which bundles the sql string and the generate-keys parameter together.
     * This constructor will always have java.sql.Statement.NO_GENERATED_KEYS as generate-keys parameter.
     * @param sqlStatementString The SQL String
     */
    public SqlStatementString(String sqlStatementString) {
        this(sqlStatementString, Statement.NO_GENERATED_KEYS);
    }

    /**
     * Prepares a PreparedStatement with the given Connection
     * @param connection The connection, it must be not null
     * @return The prepared statement
     * @throws SQLException Thrown if preparing fails through database error
     */
    public PreparedStatement prepareStatement(Connection connection) throws SQLException{
        return connection.prepareStatement(sqlStatementString, generateKeysParameter);
    }
            
    
}
