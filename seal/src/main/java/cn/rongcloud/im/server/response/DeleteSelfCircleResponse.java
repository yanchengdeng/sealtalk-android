package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/17.
 */

public class DeleteSelfCircleResponse {

    private String message;
    private int code;
    private ResultEntity data;

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

    public ResultEntity getData() {
        return data;
    }

    public void setData(ResultEntity data) {
        this.data = data;
    }

    public static class ResultEntity{

    }
}
