package ravioli.gravioli.common.postgres;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.postgresql.Driver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BasicPostgresProvider implements PostgresProvider {
    private final HikariDataSource dataSource;

    public BasicPostgresProvider(@NotNull final String host,
                                 final int port,
                                 @Nullable final String username,
                                 @Nullable final String password,
                                 @NotNull final String databaseName,
                                 @NotNull final Map<String, String> options) {
        try {
            Class.forName(Driver.class.getName());
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final HikariConfig hikariConfig = new HikariConfig();

        //postgres://postgres:123456@127.0.0.1:5432/dummy
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + databaseName);

        if (username != null) {
            hikariConfig.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
            hikariConfig.setPassword(password);
        }
        final long maxLifetime = hikariConfig.getMaxLifetime();
        final long idleTimeoutDefault = (long) (maxLifetime * 0.9);

        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(
                Integer.parseInt(options.getOrDefault("connectionTimeout", "15"))
        ));
        hikariConfig.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(
                Integer.parseInt(options.getOrDefault("leakDetectionThreshold", "60"))
        ));
        hikariConfig.setIdleTimeout(TimeUnit.SECONDS.toMillis(
                Integer.parseInt(options.getOrDefault("idleTimeout", String.valueOf(idleTimeoutDefault)))
        ));
        hikariConfig.setMinimumIdle(Integer.parseInt(options.getOrDefault("minimumIdle", "10")));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(options.getOrDefault("maximumPoolSize", "20")));
        hikariConfig.addDataSourceProperty("cachePrepStmts", options.getOrDefault("cachePrepStmts", "true"));
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", options.getOrDefault("prepStmtCacheSize", "250"));
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", options.getOrDefault("prepStmtCacheSqlLimit", "2048"));
        hikariConfig.addDataSourceProperty("zeroDateTimeBehavior", options.getOrDefault("zeroDateTimeBehavior", "convertToNull"));

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.dataSource.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeScript(@Nullable final InputStream inputStream, final boolean debug) throws IOException, SQLException {
        if (inputStream == null) {
            return;
        }
        try (final Connection connection = this.getConnection()) {
            try (inputStream; final InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {
                final ScriptRunner scriptRunner = new ScriptRunner(connection);

                if (!debug) {
                    scriptRunner.setLogWriter(null);
                }
                scriptRunner.setStopOnError(true);
                scriptRunner.setSendFullScript(false);
                scriptRunner.runScript(inputStreamReader);
            }
        }
    }
}
