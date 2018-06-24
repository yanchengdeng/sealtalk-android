package cn.rongcloud.im.server.response;

import java.io.Serializable;

/**
 * Created by lzs on 2018/6/7.
 */

public class FriendResponse implements Serializable {

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
        /* "id": 0,
  "imToken": "string",
  "isFriend": true,
  "password": "string",
  "portrait": "string",
  "remarks": "string",
  "status": "1",
  "syncName": "string",
  "userName": "string",
  "userPhone": "string"*/
        private int id;
        private String imToken;
        private String isFriend;
        private String password;
        private String remarkName;
        private String portrait;
        private String status;
        private String syncName;
        private String userName;
        private String userPhone;


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImToken() {
            return imToken;
        }

        public void setImToken(String imToken) {
            this.imToken = imToken;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public String getRemarkName() {
            return remarkName;
        }

        public void setRemarkName(String remarkName) {
            this.remarkName = remarkName;
        }

        public String getIsFriend() {
            return isFriend;
        }

        public void setIsFriend(String isFriend) {
            this.isFriend = isFriend;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSyncName() {
            return syncName;
        }

        public void setSyncName(String syncName) {
            this.syncName = syncName;
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
    }
}
