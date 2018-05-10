package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/10.
 */

public class getAddFriendResponse {

    private int code;
    private String message;
    private ResultEntity data;

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

    public ResultEntity getData() {
        return data;
    }

    public void setData(ResultEntity data) {
        this.data = data;
    }

    public static class ResultEntity{

    }
}
