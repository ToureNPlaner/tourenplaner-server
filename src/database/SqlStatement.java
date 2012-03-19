package database;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 *
 */
public class SqlStatement {
    
    private String sqlStatementString;
    private int generateKeysParameter;

    /**
     * Constructs an object which bundles the sql string and the generate-keys parameter together.
     * @param sqlStatementString The SQL String
     * @param generateKeysParameter Should be either java.sql.Statement.NO_GENERATED_KEYS or
     *                            java.sql.Statement.RETURN_GENERATED_KEYS
     */
    public SqlStatement(String sqlStatementString, int generateKeysParameter) {
        this.sqlStatementString = sqlStatementString;
        this.generateKeysParameter = generateKeysParameter;
    }

    /**
     * Constructs an object which bundles the sql string and the generate-keys parameter together.
     * This constructor will always have java.sql.Statement.NO_GENERATED_KEYS as generate-keys parameter.
     * @param sqlStatementString The SQL String
     */
    public SqlStatement(String sqlStatementString) {
        this(sqlStatementString, Statement.NO_GENERATED_KEYS);
    }

    /**
     * Creates a PreparedStatement with the given DataSource.
     * If the DataSource is created through a connection pool or similar,
     * the PreparedStatement might not be created more than one time per
     * database connection but instead retrieved from a PreparedStatement cache.
     * And do not forget to close the returned PreparedStatement with {@link #close(java.sql.PreparedStatement)},
     * this method will close the PreparedStatement and also close the connection.
     *
     * @param dataSource The data source, it must be not null
     * @return The prepared statement
     * @throws SQLException Thrown if preparing fails through database error
     */
    public PreparedStatement createPreparedStatement(DataSource dataSource) throws SQLException{
        return dataSource.getConnection().prepareStatement(sqlStatementString, generateKeysParameter);
    }


    /**
     * Closes the database connection. Exceptions will be caught.
     * @param preparedStatement the PreparedStatement to close
     */
    public static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                if (preparedStatement.getConnection() != null) {
                    preparedStatement.getConnection().close();
                }
            } catch (SQLException ignore) {
            }
            
            try {
                preparedStatement.close();
            } catch (SQLException ignore) {
            }
        }
    }

    public String getSqlStatementString() {
        return this.sqlStatementString;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SqlStatement)) {
            return false;
        }

        if (this.sqlStatementString == null) {
            if (((SqlStatement) obj).sqlStatementString != null) {
                return false;
            }
        } else {
            if (!this.sqlStatementString.equals(((SqlStatement) obj).sqlStatementString)) {
                return false;
            }
        }

        return this.generateKeysParameter == ((SqlStatement) obj).generateKeysParameter;

    }
}
