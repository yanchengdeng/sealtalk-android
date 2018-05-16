package cn.rongcloud.im.server.response;

/**
 * Created by star1209 on 2018/5/15.
 */

public class GetQiNiuTokenResponse {

    public static final int CIRCLE_TYPE = 2;
    public static final int PORTRAIT_TYPE = 1;

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
        private String folder;
        private String token;

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
