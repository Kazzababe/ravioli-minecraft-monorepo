package ravioli.gravioli.common.postgres;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public interface PostgresProvider {
    /**
     * Execute a script containing valid MySQL. If the given {@link InputStream} is {@code null}, the
     * method simply does nothing.
     *
     * @param inputStream the script to execute
     * @param debug       true to output scripts to console
     * @throws IOException  if an I/O error occurs within the InputStream
     * @throws SQLException if an error occurs while executing the script
     */
    void executeScript(@Nullable InputStream inputStream, boolean debug) throws IOException, SQLException;

    /**
     * Execute a script containing valid MySQL.
     *
     * @param inputStream The script to execute
     */
    default void executeScript(@Nullable InputStream inputStream) throws IOException, SQLException {
        this.executeScript(inputStream, false);
    }

    /**
     * Retrieve a {@link Connection} from the MySQL connection pool.
     *
     * @return a Connection from the MySQL connection pool
     * @throws SQLException if the pool is closed or there was a timeout obtaining a connection
     */
    @NotNull Connection getConnection() throws SQLException;

    /**
     * Close the data source.
     */
    void close();
}
