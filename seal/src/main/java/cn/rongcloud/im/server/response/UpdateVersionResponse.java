package cn.rongcloud.im.server.response;

/**
 * Created by lzs on 2018/5/31.
 */

public class UpdateVersionResponse {

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

    public static class ResultEntity {
        private String appUrl;
        private int id;
        private int platform;
        private int versionCode;
        private String versionTitle;

        public String getAppUrl() {
            return appUrl;
        }

        public void setAppUrl(String appUrl) {
            this.appUrl = appUrl;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getPlatform() {
            return platform;
        }

        public void setPlatform(int platform) {
            this.platform = platform;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public String getVersionTitle() {
            return versionTitle;
        }

        public void setVersionTitle(String versionTitle) {
            this.versionTitle = versionTitle;
        }
    }
}
