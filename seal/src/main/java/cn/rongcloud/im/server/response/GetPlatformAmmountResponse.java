package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/16.
 */

public class GetPlatformAmmountResponse {

    private String message;
    private String data;
    private int code;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
