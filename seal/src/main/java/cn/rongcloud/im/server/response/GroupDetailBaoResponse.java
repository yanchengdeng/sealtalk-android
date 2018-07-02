package cn.rongcloud.im.server.response;

/**
 * Author;// 邓言诚  Create at ;// 2018/6/26  11;//59
 * Email;// yanchengdeng@gmail.com
 * Describle;// 群组详情
 */
public class GroupDetailBaoResponse {
    private int code;
    private String message;

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

    public ResultEntity data;


    public class ResultEntity {
        private String createTime1;// 0,
        private String currentUserInGroup;// true,
        private String gradeLimit;// 0,
        private String groupIcon;// string,
        private String groupIntro;// string,
        private String groupMemberCount;// 0,
        private String groupName;// string,
        private String groupToken;// string,
        private String id;// 0,
        private String masterName;// string,
        private String masterSyncName;// string,
        private String nextGradeLimit;// 0,
        private String nextUpgradePrice;// 0

        public String getCreateTime1() {
            return createTime1;
        }

        public void setCreateTime1(String createTime1) {
            this.createTime1 = createTime1;
        }

        public String getCurrentUserInGroup() {
            return currentUserInGroup;
        }

        public void setCurrentUserInGroup(String currentUserInGroup) {
            this.currentUserInGroup = currentUserInGroup;
        }

        public String getGradeLimit() {
            return gradeLimit;
        }

        public void setGradeLimit(String gradeLimit) {
            this.gradeLimit = gradeLimit;
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

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public String getGroupToken() {
            return groupToken;
        }

        public void setGroupToken(String groupToken) {
            this.groupToken = groupToken;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getMasterName() {
            return masterName;
        }

        public void setMasterName(String masterName) {
            this.masterName = masterName;
        }

        public String getMasterSyncName() {
            return masterSyncName;
        }

        public void setMasterSyncName(String masterSyncName) {
            this.masterSyncName = masterSyncName;
        }

        public String getNextGradeLimit() {
            return nextGradeLimit;
        }

        public void setNextGradeLimit(String nextGradeLimit) {
            this.nextGradeLimit = nextGradeLimit;
        }

        public String getNextUpgradePrice() {
            return nextUpgradePrice;
        }

        public void setNextUpgradePrice(String nextUpgradePrice) {
            this.nextUpgradePrice = nextUpgradePrice;
        }
    }

    public ResultEntity getData() {
        return data;
    }

    public void setData(ResultEntity data) {
        this.data = data;
    }
}
