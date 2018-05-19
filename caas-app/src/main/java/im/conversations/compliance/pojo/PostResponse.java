package im.conversations.compliance.pojo;

public class PostResponse {
    private boolean success;
    private String message;
    private String redirectLink;

    public PostResponse(boolean success, String message, String redirectLink) {
        this.success = success;
        this.message = message;
        this.redirectLink = redirectLink;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getRedirectLink() {
        return redirectLink;
    }
}
