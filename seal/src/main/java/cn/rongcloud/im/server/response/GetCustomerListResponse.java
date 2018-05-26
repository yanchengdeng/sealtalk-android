package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * Created by star1209 on 2018/5/26.
 */

public class GetCustomerListResponse {

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
        private String customerName;
        private String customerID;
        private String portrait;
        private int environment;
        private long createTime1;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerID() {
            return customerID;
        }

        public void setCustomerID(String customerID) {
            this.customerID = customerID;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public int getEnvironment() {
            return environment;
        }

        public void setEnvironment(int environment) {
            this.environment = environment;
        }

        public long getCreateTime1() {
            return createTime1;
        }

        public void setCreateTime1(long createTime1) {
            this.createTime1 = createTime1;
        }
    }
}
