package cn.rongcloud.im.server.request;

/**
 * Created by star1209 on 2018/5/8.
 */

public class CompleteInfoRequest {

    private String userName;
    private String userPhone;

    public CompleteInfoRequest(String userName, String userPhone) {
        this.userName = userName;
        this.userPhone = userPhone;
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
