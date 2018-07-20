package im.conversations.compliance.pojo;

public class MailConfig {
    private String host = "localhost";
    private int port = 25;
    private String from;
    private boolean ssl = true;
    private String username = null;
    private String password = null;

    public MailConfig(String host, int port, String from, boolean ssl, String username, String password) {
        this.host = host;
        this.port = port;
        this.from = from;
        this.ssl = ssl;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getFrom() {
        return from;
    }

    public boolean getSSL() {
        return ssl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
