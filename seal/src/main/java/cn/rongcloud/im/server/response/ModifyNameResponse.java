package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/13.
 */

public class ModifyNameResponse {
    private String message;
    private int code;
    private String data;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
