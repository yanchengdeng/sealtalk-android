package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * 群 员集合
 */

public class GroupNumbersBaoResponse {

    private int code;
    private String message;
    private List<ResultEntity> data;

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

    public List<ResultEntity> getData() {
        return data;
    }

    public void setData(List<ResultEntity> data) {
        this.data = data;
    }

    public static class ResultEntity {

        private String createTime;// 2018-06-26T07;//08;//07.939Z,
        private String createTime1;// 0,
        private String id;// 0,
        private String memberRole;// 1,
        private String portrait;// string,
        private String syncName;// string,
        private String userName;// string
        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getCreateTime1() {
            return createTime1;
        }

        public void setCreateTime1(String createTime1) {
            this.createTime1 = createTime1;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(String memberRole) {
            this.memberRole = memberRole;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
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
    }
}
