package im.conversations.compliance.persistence;

import im.conversations.compliance.annotations.ComplianceTest;
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
    private static Map<String, List<HistoricalSnapshot>> historicalSnapshotsByServer;
    private static Map<String, List<HistoricalSnapshot>> historicalSnapshotsByTest;

    public static void init() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            InternalDBOperations.init(connection);
            reloadHistoricalSnapshots(connection);
        }
    }

    private static void reloadHistoricalSnapshots(Connection connection) {
        List<Server> allServers = InternalDBOperations.getAllServers(connection);
        List<Server> publicServers = InternalDBOperations.getPublicServers(connection);
        List<String> allDomains = allServers
                .stream()
                .map(it -> it.getDomain())
                .collect(Collectors.toList());
        List<String> publicDomains = publicServers
                .stream()
                .map(it -> it.getDomain())
                .collect(Collectors.toList());
        historicalSnapshotsByServer = InternalDBOperations.getHistoricalSnapshotsGroupedByServer(connection, TestUtils.getTestNames(), allDomains);
        historicalSnapshotsByTest = InternalDBOperations.getHistoricalSnapshotGroupedByTest(connection, TestUtils.getAllTestNames(), publicDomains);
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
        List<Server> servers;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            if (onlyPublic) {
                servers = InternalDBOperations.getPublicServers(connection);
            } else {
                servers = InternalDBOperations.getAllServers(connection);
            }
        }
        return servers;
    }

    public static Optional<Server> getServer(String domain) {
        Server server;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            server = InternalDBOperations.getServer(connection, domain);
        }
        return Optional.ofNullable(server);
    }

    public static boolean removeServer(Server server) {
        boolean success;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            success = InternalDBOperations.removeServer(connection, server);
        }
        return success;
    }

    public static Iteration getIteration(int iterationNumber) {
        Iteration iteration;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            iteration = InternalDBOperations.getIteration(connection, iterationNumber);
        }
        return iteration;
    }

    public static Optional<Iteration> getLatestIteration() {
        Iteration iteration;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            iteration = InternalDBOperations.getLatestIteration(connection);
        }
        return Optional.ofNullable(iteration);
    }

    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByServer() {
        return Collections.unmodifiableMap(historicalSnapshotsByServer);
    }

    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByTest() {
        return Collections.unmodifiableMap(historicalSnapshotsByTest);
    }

    public static boolean addCredential(Credential credential) {
        boolean success;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            success = InternalDBOperations.addCredential(connection, credential);
        }
        return success;
    }

    public static Credential getCredentialFor(String domain) {
        Credential credential;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            credential = InternalDBOperations.getCredentialFor(connection, domain);
        }
        return credential;
    }

    public static List<Credential> getCredentials() {
        List<Credential> credentials;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            credentials = InternalDBOperations.getCredentials(connection);
        }
        return credentials;
    }

    public static boolean removeCredential(Credential credential) {
        boolean status;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            status = InternalDBOperations.removeCredential(connection, credential);
        }
        return status;
    }

    public static boolean addCurrentResults(String domain, List<Result> results, Instant timestamp) {
        boolean success;
        try (Connection connection = DBConnections.getInstance().getConnection(true)) {
            success = InternalDBOperations.addCurrentResults(connection, domain, results, timestamp);
            connection.commit();
        }
        return success;
    }

    public static boolean addPeriodicResults(List<ResultDomainPair> rdpList, Instant beginTime, Instant endTime) {
        boolean success;
        try (Connection connection = DBConnections.getInstance().getConnection(true)) {
            success = InternalDBOperations.addPeriodicResults(connection, rdpList, beginTime, endTime);
            if (success) {
                reloadHistoricalSnapshots(connection);
            }
            connection.commit();
        }
        return success;
    }

    /**
     * Get results for public servers
     * @return
     */
    public static Map<String, HashMap<String, Boolean>> getCurrentResultsByServer() {
        Map<String, HashMap<String, Boolean>> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getCurrentResultsByServer(connection);
        }
        return results;
    }

    public static List<Result> getCurrentResultsForServer(String domain) {
        List<Result> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getCurrentResultsForServer(connection, domain);
        }
        return results;
    }

    public static Map<String, HashMap<String, Boolean>> getCurrentResultsByTest() {
        Map<String, HashMap<String, Boolean>> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getCurrentResultsByTest(connection);
        }
        return results;
    }

    public static HashMap<String, Boolean> getCurrentResultsForTest(ComplianceTest test) {
        HashMap<String, Boolean> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getCurrentResultsForTest(connection, test.short_name());
        }
        return results;
    }

    public static Instant getLastRunFor(String domain) {
        Instant instant;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            instant = InternalDBOperations.getLastRunFor(connection, domain);
        }
        return instant;
    }

    public static List<Result> getHistoricalResultsFor(String domain, int iterationNumber) {
        List<Result> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getHistoricalResultsFor(connection, domain, iterationNumber);
        }
        return results;
    }

    public static boolean setListed(String domain, boolean listedServer) {
        boolean status = false;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            status = InternalDBOperations.setListed(connection, domain, listedServer);
        }
        return status;
    }

    public static boolean addSubscriber(Subscriber subscriber) {
        boolean status = false;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            status = InternalDBOperations.addSubscriber(connection, subscriber);
        }
        return status;
    }

    public static List<Subscriber> getSubscribersFor(String domain) {
        List<Subscriber> subscribers;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            subscribers = InternalDBOperations.getSubscribersFor(connection, domain);
        }
        return subscribers;
    }

    public static Subscriber removeSubscriber(String unsubscribeCode) {
        Subscriber subscriber;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            subscriber = InternalDBOperations.removeSubscriber(connection, unsubscribeCode);
        }
        return subscriber;
    }
}
