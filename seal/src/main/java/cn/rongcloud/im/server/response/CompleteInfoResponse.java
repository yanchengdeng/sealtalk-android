package cn.rongcloud.im.server.response;

import java.io.Serializable;

/**
 * Created by star1209 on 2018/5/8.
 */

public class CompleteInfoResponse {

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

    public static class ResultEntity {

        private long id;
        private String userName;
        private String userPhone;
        private String password;
        private String syncName;
        private int status;
        private String imToken;

        public String getImToken() {
            return imToken;
        }

        public void setImToken(String imToken) {
            this.imToken = imToken;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserPhone() {
            return userPhone;
        }

        public void setUserPhone(String userPhone) {
            this.userPhone = userPhone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSyncName() {
            return syncName;
        }

        public void setSyncName(String syncName) {
            this.syncName = syncName;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
