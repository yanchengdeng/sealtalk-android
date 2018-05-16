package cn.rongcloud.im.message.plugins;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.RLog;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * Created by star1209 on 2018/5/15.
 */

@MessageTag(value = "RC:TransferMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class TransferMessage extends MessageContent {

    private static final String TAG = "TransferMessage";

    private double money;
    private String userName;
    private String syncName;
    private String leaveWord;
    private String portrait;

    public TransferMessage(double money, String userName, String syncName, String leaveWord, String portrait){
        this.money = money;
        this.userName = userName;
        this.syncName = syncName;
        this.leaveWord = leaveWord;
        this.portrait = portrait;
    }

    public TransferMessage(Parcel in) {
        money = in.readDouble();
        userName = in.readString();
        syncName = in.readString();
        leaveWord = in.readString();
        portrait = in.readString();
    }

    public TransferMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if (jsonObj.has("syncName"))
                setSyncName(jsonObj.optString("syncName"));
            if (jsonObj.has("userName"))
                setUserName(jsonObj.optString("userName"));
            if (jsonObj.has("money"))
                setMoney(jsonObj.optDouble("money"));
            if (jsonObj.has("leaveWord"))
                setLeaveWord(jsonObj.optString("leaveWord"));
            if (jsonObj.has("portrait"))
                setPortrait(jsonObj.optString("portrait"));
            if (jsonObj.has("user"))
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
        } catch (JSONException e) {
            RLog.e(TAG, "JSONException " + e.getMessage());
        }
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("syncName", getSyncName());
            jsonObject.put("userName", getUserName());
            jsonObject.put("money", getMoney());
            jsonObject.put("leaveWord", getLeaveWord());
            jsonObject.put("portrait", getPortrait());
            if (getJSONUserInfo() != null)
                jsonObject.putOpt("user", getJSONUserInfo());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        try {
            return jsonObject.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(money);
        dest.writeString(userName);
        dest.writeString(syncName);
        dest.writeString(leaveWord);
        dest.writeString(portrait);
    }

    public static final Creator<TransferMessage> CREATOR = new Creator<TransferMessage>() {
        @Override
        public TransferMessage createFromParcel(Parcel source) {
            return new TransferMessage(source);
        }

        @Override
        public TransferMessage[] newArray(int size) {
            return new TransferMessage[size];
        }
    };

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSyncName() {
        return syncName;
    }

    public void setSyncName(String syncName) {
        this.syncName = syncName;
    }

    public String getLeaveWord() {
        return leaveWord;
    }

    public void setLeaveWord(String leaveWord) {
        this.leaveWord = leaveWord;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
}
