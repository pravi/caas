We welcome all improvements, contributions to this project. Whether you are an XMPP enthusiast, server maintainer or web developer, you can always add something of value to this project.

### Add or update instructions for passing tests for a server
Write appropriate instructions, by editing caas-web/src/main/resources/help/server_name/test_name.md, or create if missing.
If you are adding a new server, go to caas-web/src/test/java/im/conversations/compliance/HelpsTest.java and add a test to check if valid helps exist for your server.

### Writing new tests or modifying existing ones
We use [Babbler XMPP library](https://sco0ter.bitbucket.io/babbler/) for all our XMPP related tasks.
You can read its documentation to get started. Tests are stored in caas-app/src/main/java/im/conversations/compliance/xmpp/tests/Test_Name.java
Or you can alternatively just modify the annotations for tests if the metadata of test is incorrect.
Note: You __MUST NOT__ change the short_name of an existing test.

### Adding a new feature to the website
We use [Spark web framework](http://sparkjava.com/) and Freemarker Template engine for the website. You can find all the HTML/CSS/JS in caas-web/src/main/resources

Have a fun time hacking :)
