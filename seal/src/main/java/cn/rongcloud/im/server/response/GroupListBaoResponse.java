package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * 群列表
 */

public class GroupListBaoResponse {

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

        private String id;// 20,
        private String groupToken;// 96e5120b21dd461bb75991da665d464d,
        private String groupName;// 哦啦啦,
        private String groupIcon;// dfsdf,
        private String groupIntro;// OK了啦,
        private String groupMemberCount;// 1,
        private String createTime1;// 1529913327000

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGroupToken() {
            return groupToken;
        }

        public void setGroupToken(String groupToken) {
            this.groupToken = groupToken;
        }

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupIcon() {
            return groupIcon;
        }

        public void setGroupIcon(String groupIcon) {
            this.groupIcon = groupIcon;
        }

        public String getGroupIntro() {
            return groupIntro;
        }

        public void setGroupIntro(String groupIntro) {
            this.groupIntro = groupIntro;
        }

        public String getGroupMemberCount() {
            return groupMemberCount;
        }

        public void setGroupMemberCount(String groupMemberCount) {
            this.groupMemberCount = groupMemberCount;
        }

        public String getCreateTime1() {
            return createTime1;
        }

        public void setCreateTime1(String createTime1) {
            this.createTime1 = createTime1;
        }
    }
}
