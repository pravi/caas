package im.conversations.compliance.persistence;

import com.zaxxer.hikari.HikariDataSource;
import im.conversations.compliance.pojo.Configuration;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import static java.sql.Connection.TRANSACTION_SERIALIZABLE;

public class DBConnections {
    private static DBConnections INSTANCE;
    private static boolean init;
    private final Sql2o database;
    private String dbUrl;

    private DBConnections(String dbUrl) {
        this.dbUrl = dbUrl;
        if (dbUrl == null) {
            dbUrl = Configuration.getInstance().getDBUrl().orElse("jdbc:sqlite:data.db");
        }
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(Configuration.getInstance().getDBConnections());
        dataSource.setJdbcUrl(dbUrl);
        database = new Sql2o(dataSource);
    }

    public static void init() {
        init(null);
    }

    public static void init(String dbUrl) {
        if (init) {
            throw new IllegalStateException("DBConnections has already been initialised");
        }
        init = true;
        INSTANCE = new DBConnections(dbUrl);
    }

    public static DBConnections getInstance() {
        return INSTANCE;
    }

    public Connection getConnection(boolean serialisable) {
        if (serialisable) {
            return database.beginTransaction(TRANSACTION_SERIALIZABLE);
        } else {
            return database.open();
        }
    }
}
