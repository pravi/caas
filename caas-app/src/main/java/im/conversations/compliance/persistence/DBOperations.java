package im.conversations.compliance.persistence;

import im.conversations.compliance.pojo.*;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.sql2o.Connection;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper around {@link InternalDBOperations}, which handles connections, stores historical snapshots in memory
 * reducing database load
 */
public class DBOperations {
    private static Map<String, List<HistoricalSnapshot>> historicSnapshotsByServer;
    private static Map<String, List<HistoricalSnapshot>> historicSnapshotsByTest;

    public static void init() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            InternalDBOperations.init(connection);
            reloadHistoricalSnapshots(connection);
        }
    }

    private static void reloadHistoricalSnapshots(Connection connection) {
        List<Server> servers = InternalDBOperations.getServers(connection);
        List<String> allDomains = servers
                .stream()
                .map(it -> it.getDomain())
                .collect(Collectors.toList());
        List<String> publicDomains = servers
                .stream()
                .map(it -> it.getDomain())
                .collect(Collectors.toList());
        historicSnapshotsByServer = InternalDBOperations.getHistoricResultsGroupedByServer(connection, TestUtils.getTestNames(), allDomains);
        historicSnapshotsByTest = InternalDBOperations.getHistoricResultsGroupedByTest(connection, TestUtils.getAllTestNames(), publicDomains);
    }

    public static boolean addServer(Server server) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            boolean success = InternalDBOperations.addServer(connection, server);
            return success;
        }
    }

    public static boolean updateServer(Server server) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            boolean success = InternalDBOperations.updateServer(connection, server);
            return success;
        }
    }

    /**
     * Get a list of servers
     *
     * @param onlyPublic Should only public servers be returned
     * @return A list of servers
     */
    public static List<Server> getServers(boolean onlyPublic) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            List<Server> servers = InternalDBOperations.getServers(connection);
            if (onlyPublic) {
                return servers
                        .stream()
                        .filter(Server::isListed)
                        .collect(Collectors.toList());
            }
            return servers;
        }
    }

    public static Optional<Server> getServer(String domain) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Server server = InternalDBOperations.getServer(connection, domain);
            return Optional.ofNullable(server);
        }
    }

    public static boolean removeServer(Server server) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            boolean success = InternalDBOperations.removeServer(connection, server);
            return success;
        }
    }

    public static Iteration getIteration(int iterationNumber) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Iteration iteration = InternalDBOperations.getIteration(connection, iterationNumber);
            return iteration;
        }
    }

    public static Optional<Iteration> getLatestIteration() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Iteration iteration;
            iteration = InternalDBOperations.getLatestIteration(connection);
            return Optional.ofNullable(iteration);
        }
    }

    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByServer() {
        return Collections.unmodifiableMap(historicSnapshotsByServer);
    }

    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByTest() {
        return Collections.unmodifiableMap(historicSnapshotsByTest);
    }

    public static boolean addCredential(Credential credential) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            boolean success = InternalDBOperations.addCredential(connection, credential);
            return success;
        }
    }

    public static Credential getCredentialFor(String domain) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Credential credential = InternalDBOperations.getCredentialFor(connection, domain);
            return credential;
        }
    }

    public static List<Credential> getCredentials() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            List<Credential> credentials = InternalDBOperations.getCredentials(connection);
            return credentials;
        }
    }

    public static boolean removeCredential(Credential credential) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            boolean status = InternalDBOperations.removeCredential(connection, credential);
            return status;
        }
    }

    public static boolean addCurrentResults(String domain, List<Result> results, Instant timestamp) {
        try (Connection connection = DBConnections.getInstance().getConnection(true)) {
            boolean success = InternalDBOperations.addCurrentResults(connection, domain, results, timestamp);
            connection.commit();
            connection.close();
            return success;
        }
    }

    public static boolean addPeriodicResults(List<ResultDomainPair> rdpList, Instant beginTime, Instant endTime) {
        try (Connection connection = DBConnections.getInstance().getConnection(true)) {
            boolean success = InternalDBOperations.addPeriodicResults(connection, rdpList, beginTime, endTime);
            if (success) {
                reloadHistoricalSnapshots(connection);
            }
            connection.commit();
            connection.close();
            return success;
        }
    }

    public static Map<String, HashMap<String, Boolean>> getCurrentResultsHashMapByServer() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            return InternalDBOperations.getCurrentResultsHashMapByServer(connection);
        }
    }

    public static Map<String, List<Result>> getCurrentResultsByServer() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Map<String, List<Result>> results = InternalDBOperations.getCurrentResultsByServer(connection);
            return results;
        }
    }

    public static Map<String, HashMap<String, Boolean>> getCurrentResultsByTest() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Map<String, HashMap<String, Boolean>> results = InternalDBOperations.getCurrentResultsByTest(connection);
            return results;
        }
    }

    public static Instant getLastRunFor(String domain) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            Instant instant = InternalDBOperations.getLastRunFor(connection, domain);
            return instant;
        }
    }

    public static List<Result> getHistoricalResultsFor(String domain, int iterationNumber) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            List<Result> results = InternalDBOperations.getHistoricalResultsFor(connection, domain, iterationNumber);
            return results;
        }
    }

}
