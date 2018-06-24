package cn.rongcloud.im.server.response;

/**
 * Created by AMing on 16/2/17.
 * Company RongCloud
 */
public class SetFriendDisplayNameResponse {

    private int code;
    private String message;

    private EntryData data;

    public class EntryData {

    }

    public EntryData getData() {
        return data;
    }

    public void setData(EntryData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
