package cn.rongcloud.im.model;

/**
 * Created by lzs on 2018/6/7.
 */

public class QRCodeBean {

    private int type;
    private String userId;
    private String groupId;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
