package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/14.
 */

public class UploadPortraitResponse {

    private int code;
    private String message;
    private String data;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
