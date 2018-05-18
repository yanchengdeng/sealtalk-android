package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * Created by star1209 on 2018/5/17.
 */

public class GetTransferHistoryResponse {

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
        private double amount;
        private long userId;
        private String userName;
        private long acceptTime1;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public long getAcceptTime1() {
            return acceptTime1;
        }

        public void setAcceptTime1(long acceptTime1) {
            this.acceptTime1 = acceptTime1;
        }
    }
}
