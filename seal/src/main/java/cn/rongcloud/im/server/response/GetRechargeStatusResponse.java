package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/16.
 */

public class GetRechargeStatusResponse {

    private String message;
    private int code;
    private double data;

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

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }
}
