package cn.rongcloud.im.server.response;

import java.io.Serializable;

/**
 * Created by lzs on 2018/6/7.
 */

public class AdResponse implements Serializable {

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
  {"data":{"id":1,"imagesPath":"http://7vztps.com1.z0.glb.clouddn.com/FuUXezn8ulpOJN1AQjgd8XvDr0F_","showTime":8},"code":100000,"message":"执行成功."}
  "status": "1",
  "syncName": "string",
  "userName": "string",
  "userPhone": "string"*/
        private int id;
        private String imagesPath;
        private long showTime;

        public long getShowTime() {
            return showTime;
        }

        public void setShowTime(long showTime) {
            this.showTime = showTime;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getImagesPath() {
            return imagesPath;
        }

        public void setImagesPath(String imagesPath) {
            this.imagesPath = imagesPath;
        }
    }
}
