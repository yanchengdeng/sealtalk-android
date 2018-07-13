package cn.rongcloud.im.server.response;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lzs on 2018/6/7.
 */

public class AccountHistoryResponse implements Serializable {

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
        private double amount;//0,
        private String balance;//0,
        private String fundDirection;//4,
        private String trxNo;//cc6e97721b474af4902d5f9f007bc832,
        private String trxType;//红包,
        private String createTime;//2018-06-08T16;//03;//24.000+0000,
        private long createTime1;//1528473804000

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getBalance() {
            return balance;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public String getFundDirection() {
            return fundDirection;
        }

        public void setFundDirection(String fundDirection) {
            this.fundDirection = fundDirection;
        }

        public String getTrxNo() {
            return trxNo;
        }

        public void setTrxNo(String trxNo) {
            this.trxNo = trxNo;
        }

        public String getTrxType() {
            return trxType;
        }

        public void setTrxType(String trxType) {
            this.trxType = trxType;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public long getCreateTime1() {
            return createTime1;
        }

        public void setCreateTime1(long createTime1) {
            this.createTime1 = createTime1;
        }
    }
}
