package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/15.
 */

public class GetMineAmountResponse {

    private int data;
    private String message;
    private int code;

    public int getData() {
        return data;
    }

    public void setData(int data) {
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
