package cn.rongcloud.im.server.response;

import java.io.Serializable;

/**
 * Created by lzs on 2018/6/7.
 */

public class PersonCenterPicResponse implements Serializable {

    private int code;
    private String message;
    private String[] data;

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

    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }
}
