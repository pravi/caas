package im.conversations.compliance.persistence;

import com.zaxxer.hikari.HikariDataSource;
import im.conversations.compliance.pojo.Configuration;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Optional;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class DBConnections {
    private static DBConnections INSTANCE;
    private static boolean init;
    private final Sql2o database;

    private DBConnections() {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(Configuration.getInstance().getDBUrl());
        dataSource.setJdbcUrl(Configuration.getInstance().getDBUrl());
        Optional<Configuration.DbCredentials> credentials = Configuration.getInstance().getDbCredentials();
        if (credentials.isPresent()) {
            dataSource.setPassword(credentials.get().password);
            dataSource.setUsername(credentials.get().username);
        }
        dataSource.setMaximumPoolSize(Configuration.getInstance().getDBConnections());
        database = new Sql2o(dataSource);
    }

    public static void init() {
        if (init) {
            throw new IllegalStateException("DBConnections has already been initialised");
        }
        init = true;
        INSTANCE = new DBConnections();
    }

    public static DBConnections getInstance() {
        return INSTANCE;
    }

    public Connection getConnection(boolean serializable) {
        if (serializable) {
            return database.beginTransaction(TRANSACTION_SERIALIZABLE);
        } else {
            return database.open();
        }
    }
}
