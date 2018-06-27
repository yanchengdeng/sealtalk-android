package cn.rongcloud.im.server.response;

/**
 * Created by AMing on 16/1/29.
 * Company RongCloud
 */
public class DismissGroupResponse {
    private int code;

    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
