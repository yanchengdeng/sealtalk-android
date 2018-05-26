package cn.rongcloud.im.server.request;

/**
 * Created by star1209 on 2018/5/8.
 */

public class CompleteInfoRequest {

    private String userName;
    private String syncName;

    public CompleteInfoRequest(String userName, String userPhone) {
        this.userName = userName;
        this.syncName = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSyncName() {
        return syncName;
    }

    public void setSyncName(String syncName) {
        this.syncName = syncName;
    }
}
