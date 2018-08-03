package im.conversations.compliance;

import im.conversations.compliance.persistence.InternalDBOperations;
import im.conversations.compliance.pojo.Subscriber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SubscribersTest {
    private static final String JDBC_URL = "jdbc:sqlite::memory:";
    private Sql2o database;
    private Connection connection;

    @Before()
    public void init() {
        database = new Sql2o(JDBC_URL, null, null);
        connection = database.open();
        InternalDBOperations.init(connection);
    }

    @Test
    public void whenNoSubscribers() {
        Assert.assertTrue(
                InternalDBOperations.getSubscribersFor(connection, "dummy")
                        .isEmpty()
        );
        Assert.assertNull(
                InternalDBOperations.removeSubscriber(
                        connection, "test"
                )
        );
        Assert.assertFalse(
                InternalDBOperations.isSubscribed(
                        connection,
                        "dummy@example.com",
                        "example.com"
                ));
    }

    @Test
    public void checkGetSubscribers() {
        String email1 = "someone1@example.com";
        String email2 = "someone2@example.com";
        String domain1 = "example.com";
        String domain2 = "dummy.com";

        Subscriber subscriber1 = Subscriber.createSubscriber(email1, domain1);
        InternalDBOperations.addSubscriber(connection, subscriber1);
        List<Subscriber> subscribers = InternalDBOperations.getSubscribersFor(connection, domain1);
        Assert.assertEquals(Collections.singletonList(subscriber1), subscribers);
        Assert.assertTrue(InternalDBOperations.getSubscribersFor(connection, domain2).isEmpty());

        Subscriber subscriber2 = Subscriber.createSubscriber(email2, domain1);
        InternalDBOperations.addSubscriber(connection, subscriber2);
        subscribers = InternalDBOperations.getSubscribersFor(connection, domain1);
        Assert.assertEquals(Arrays.asList(subscriber1, subscriber2), subscribers);
    }

    public void checkUnsubscribe() {
        String domain1 = "example.com";
        String domain2 = "example2.com";
        Subscriber subscriber1 = Subscriber.createSubscriber("test1@example.com", domain1);
        Subscriber subscriber2 = Subscriber.createSubscriber("test2@example.com", domain2);
        Subscriber subscriber3 = Subscriber.createSubscriber("test3@example.com", domain1);
        InternalDBOperations.addSubscriber(connection, subscriber1);
        InternalDBOperations.addSubscriber(connection, subscriber2);
        InternalDBOperations.addSubscriber(connection, subscriber3);
        String code = subscriber1.getUnsubscribeCode();
        Subscriber readBackSubscriber1 = InternalDBOperations.removeSubscriber(connection, code);
        Assert.assertEquals(subscriber1, readBackSubscriber1);
        List<Subscriber> readBackSubscribers = InternalDBOperations.getSubscribersFor(connection, domain1);
        Assert.assertEquals(Collections.singletonList(subscriber2),readBackSubscribers);
    }

    public void isSubscribedTest() {
        String email1 = "test1@exmaple.com";
        String email2 = "test2@exmaple.com";
        String domain1 = "jabber-server-1.com";
        String domain2 = "jabber-server-2.com";
        Subscriber subscriber1 = Subscriber.createSubscriber(email1, domain1);
        Subscriber subscriber2 = Subscriber.createSubscriber(email1, domain2);
        Subscriber subscriber3 = Subscriber.createSubscriber(email2, domain2);
        InternalDBOperations.addSubscriber(connection, subscriber1);
        InternalDBOperations.addSubscriber(connection, subscriber1);
        InternalDBOperations.addSubscriber(connection, subscriber2);
        InternalDBOperations.addSubscriber(connection, subscriber3);
        Assert.assertTrue(InternalDBOperations.isSubscribed(connection, email1, domain1));
        Assert.assertTrue(InternalDBOperations.isSubscribed(connection, email1, domain2));
        Assert.assertFalse(InternalDBOperations.isSubscribed(connection, email2, domain1));
        Assert.assertTrue(InternalDBOperations.isSubscribed(connection, email2, domain2));
    }
}
