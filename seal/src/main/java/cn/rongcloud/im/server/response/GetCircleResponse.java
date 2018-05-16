package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * Created by star1209 on 2018/5/14.
 */

public class GetCircleResponse {

    private String message;
    private int code;
    private List<ResultEntity> data;

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

    public List<ResultEntity> getData() {
        return data;
    }

    public void setData(List<ResultEntity> data) {
        this.data = data;
    }

    public static class ResultEntity{
        private long id;
        private int circleType;
        private String content;
        private List<String> circleImagePath;
        private String portrait;
        private String userName;
        private long publishTime;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getCircleType() {
            return circleType;
        }

        public void setCircleType(int circleType) {
            this.circleType = circleType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getCircleImagePath() {
            return circleImagePath;
        }

        public void setCircleImagePath(List<String> circleImagePath) {
            this.circleImagePath = circleImagePath;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public long getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(long publishTime) {
            this.publishTime = publishTime;
        }
    }
}