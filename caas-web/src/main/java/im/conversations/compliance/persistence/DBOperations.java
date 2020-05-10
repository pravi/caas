package im.conversations.compliance.persistence;

import com.google.common.base.Stopwatch;
import im.conversations.compliance.annotations.ComplianceTest;
import im.conversations.compliance.pojo.*;
import im.conversations.compliance.xmpp.utils.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A wrapper around {@link InternalDBOperations}, which handles connections, stores historical snapshots in memory
 * reducing database load
 */
public class DBOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBOperations.class);

    private static Map<String, List<HistoricalSnapshot>> historicalSnapshotsByServer;
    private static Map<String, List<HistoricalSnapshot>> historicalSnapshotsByTest;

    public static void init() {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            //InternalDBOperations.init(connection);
            reloadHistoricalSnapshots(connection);
        }
    }

    private static void reloadHistoricalSnapshots(Connection connection) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("reloading historical snapshots");
        List<Server> allServers = InternalDBOperations.getAllServers(connection);
        List<Server> publicServers = InternalDBOperations.getPublicServers(connection);
        List<String> allDomains = allServers
                .stream()
                .map(Server::getDomain)
                .collect(Collectors.toList());
        List<String> publicDomains = publicServers
                .stream()
                .map(Server::getDomain)
                .collect(Collectors.toList());
        LOGGER.info("getting by server for {} domains", allDomains.size());
        historicalSnapshotsByServer = InternalDBOperations.getHistoricalSnapshotsGroupedByServer(connection, TestUtils.getTestNames(), allDomains);
        LOGGER.info("getting by test for {} domains", publicDomains.size());
        historicalSnapshotsByTest = InternalDBOperations.getHistoricalSnapshotGroupedByTest(connection, TestUtils.getAllTestNames(), publicDomains);
        LOGGER.info("reloading historical snapshots took {}", stopwatch.stop().elapsed());
    }

    public static boolean addServer(Server server) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            return InternalDBOperations.addServer(connection, server);
        }
    }

    public static boolean updateServer(Server server) {
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            return InternalDBOperations.updateServer(connection, server);
        }
    }

    /**
     * Get a list of fully compliant listed servers, which support in band registration
     * @return
     */
    public static List<String> getCompliantServers() {
        List<String> servers;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            servers = InternalDBOperations.getCompliantServers(connection);
        }
        return servers;
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
        if (historicalSnapshotsByServer == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(historicalSnapshotsByServer);
    }

    public static Map<String, List<HistoricalSnapshot>> getHistoricResultsGroupedByTest() {
        if (historicalSnapshotsByTest == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(historicalSnapshotsByTest);
    }

    public static boolean addCredential(Credential credential) {
        boolean success;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            success = InternalDBOperations.addCredential(connection, credential);
        }
        return success;
    }

    public static Optional<Credential> getCredentialFor(String domain) {
        Credential credential;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            credential = InternalDBOperations.getCredentialFor(connection, domain);
        }
        return Optional.ofNullable(credential);
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

     public static void setFailure(Credential credential, String reason) {
        final String query = "UPDATE credentials set failures = failures +1, failure_reason=:reason where jid=:jid and password=:password";
         try (Connection connection = DBConnections.getInstance().getConnection(false)) {
             connection.createQuery(query).addParameter("reason", reason).addParameter("jid", credential.getJid()).addParameter("password", credential.getPassword()).executeUpdate();
         }
    }

    public static void setSuccess(Credential credential) {
         final String query = "UPDATE credentials set failures = 0, failure_reason=NULL where jid=:jid and password=:password";
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            connection.createQuery(query).addParameter("jid", credential.getJid()).addParameter("password", credential.getPassword()).executeUpdate();
        }
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

    public static Map<String, Boolean> getCurrentResultsForTest(ComplianceTest test) {
        Map<String, Boolean> results;
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

    public static Map<String, HashMap<String, Boolean>> getHistoricalTableFor(int iterationNumber) {
        Map<String, HashMap<String, Boolean>> results;
        try (Connection connection = DBConnections.getInstance().getConnection(false)) {
            results = InternalDBOperations.getHistoricalTableFor(connection, iterationNumber);
        }
        return results;
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

    public static int deleteFailedCredentials() {
        try (final Connection connection = DBConnections.getInstance().getConnection(false)) {
            return InternalDBOperations.deleteFailedCredentials(connection);
        }
    }
}
