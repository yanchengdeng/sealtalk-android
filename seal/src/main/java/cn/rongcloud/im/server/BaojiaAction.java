package cn.rongcloud.im.server;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.entity.StringEntity;

import java.io.UnsupportedEncodingException;

import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.request.CompleteInfoRequest;
import cn.rongcloud.im.server.response.CompleteInfoResponse;
import cn.rongcloud.im.server.response.GetLoginStatusResponse;
import cn.rongcloud.im.server.response.SearchContactResponse;
import cn.rongcloud.im.server.response.getAddFriendResponse;
import io.rong.common.RLog;

/**
 * Created by star1209 on 2018/5/7.
 */

public class BaojiaAction extends BaseAction {

    public static String BASE_URL = "http://api.baojia.co";
    private final String CONTENT_TYPE = "application/json";
    private final String ENCODING = "utf-8";

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public BaojiaAction(Context context) {
        super(context);
    }

    /**
     * 获取登录状态
     * @param transId
     * @return
     * @throws HttpException
     */
    public GetLoginStatusResponse getLoginStatus(String transId) throws HttpException {
        String url = String.format(BASE_URL + "/user/login/check?transId=%s", transId);
        RLog.v("GetLoginStatusResponse", url);
        String response = httpManager.post(url);
        RLog.v("GetLoginStatusResponse", response);
        GetLoginStatusResponse statusResponse = null;
        if (!TextUtils.isEmpty(response)){
            statusResponse = jsonToBean(response, GetLoginStatusResponse.class);
        }

        return statusResponse;
    }

    /**
     * 用户补充资料
     * @param name
     * @param phone
     * @return
     */
    public CompleteInfoResponse completeInfo(String syncName, String name, String phone) throws HttpException {
        String url = String.format(BASE_URL + "/user/register/%s", syncName);
        RLog.v("CompleteInfoResponse", url);
        StringEntity entity = null;
        try {
            entity = new StringEntity(BeanTojson(new CompleteInfoRequest(name, phone)), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String response = httpManager.post(mContext, url, entity, CONTENT_TYPE);
        RLog.v("CompleteInfoResponse", response);
        CompleteInfoResponse infoResponse = null;
        if (!TextUtils.isEmpty(response)){
            infoResponse = jsonToBean(response, CompleteInfoResponse.class);
        }

        return infoResponse;
    }

    /**
     * 搜索联系人
     * @param sycnName
     * @return
     * @throws HttpException
     */
    public SearchContactResponse searchContact(String sycnName) throws HttpException {
        String url = String.format(BASE_URL + "/user/query/keyword?keyword=%s", sycnName);
        RLog.v("searchContact", url);
        String response = httpManager.post(url);
        RLog.v("searchContact", response);
        SearchContactResponse searchResponse = null;
        if (!TextUtils.isEmpty(response)){
            searchResponse = jsonToBean(response, SearchContactResponse.class);
        }

        return searchResponse;
    }

    /**
     * 添加好友邀请
     * @param user
     * @param friend
     * @return
     * @throws HttpException
     */
    public getAddFriendResponse addFriend(String user, String friend) throws HttpException {
        String url = String.format(BASE_URL + "/friend/request?friendname=%s&username=%s", friend, user);
        RLog.v("getAddFriendResponse", url);
        String response = httpManager.get(url);
        RLog.v("getAddFriendResponse", response);
        getAddFriendResponse getResponse = null;
        if (!TextUtils.isEmpty(response)){
            getResponse = jsonToBean(response, getAddFriendResponse.class);
        }

        return getResponse;
    }
}
