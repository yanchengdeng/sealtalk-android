package cn.rongcloud.im.server;

import android.content.Context;
import android.text.TextUtils;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import cn.rongcloud.im.server.network.http.HttpException;
import cn.rongcloud.im.server.request.CompleteInfoRequest;
import cn.rongcloud.im.server.response.AdResponse;
import cn.rongcloud.im.server.response.AddGroupMemberResponse;
import cn.rongcloud.im.server.response.AgreeFriendResponse;
import cn.rongcloud.im.server.response.CompleteInfoResponse;
import cn.rongcloud.im.server.response.CreateGroupBaoResponse;
import cn.rongcloud.im.server.response.DeleteContactResponse;
import cn.rongcloud.im.server.response.DeleteGroupMemberResponse;
import cn.rongcloud.im.server.response.DeleteSelfCircleResponse;
import cn.rongcloud.im.server.response.FriendInvitationResponse;
import cn.rongcloud.im.server.response.FriendResponse;
import cn.rongcloud.im.server.response.GetCircleResponse;
import cn.rongcloud.im.server.response.GetCustomerListResponse;
import cn.rongcloud.im.server.response.GetFriendResponse;
import cn.rongcloud.im.server.response.GetLoginStatusResponse;
import cn.rongcloud.im.server.response.GetMineAmountResponse;
import cn.rongcloud.im.server.response.GetPlatformAmmountResponse;
import cn.rongcloud.im.server.response.GetQiNiuTokenResponse;
import cn.rongcloud.im.server.response.GetRechargeStatusResponse;
import cn.rongcloud.im.server.response.GetRelationFriendResponse;
import cn.rongcloud.im.server.response.GetTransferAggregationResponse;
import cn.rongcloud.im.server.response.GetTransferHistoryResponse;
import cn.rongcloud.im.server.response.GroupDetailBaoResponse;
import cn.rongcloud.im.server.response.GroupListBaoResponse;
import cn.rongcloud.im.server.response.GroupNumbersBaoResponse;
import cn.rongcloud.im.server.response.ModifyNameResponse;
import cn.rongcloud.im.server.response.ModifyPortraitResponse;
import cn.rongcloud.im.server.response.PublishCircleResponse;
import cn.rongcloud.im.server.response.QuitGroupResponse;
import cn.rongcloud.im.server.response.SearchContactListResponse;
import cn.rongcloud.im.server.response.SearchContactResponse;
import cn.rongcloud.im.server.response.SetFriendDisplayNameResponse;
import cn.rongcloud.im.server.response.TransferResponse;
import cn.rongcloud.im.server.response.UpdateVersionResponse;
import cn.rongcloud.im.server.response.getAddFriendResponse;
import io.rong.common.RLog;

/**
 * Created by star1209 on 2018/5/7.
 */

public class BaojiaAction extends BaseAction {

    public static String BASE_URL = "http://api.baojia.co";//正式服务器
    //    public static final String BASE_URL = "http://13.228.95.208:9091"; //测试服务器
    private final String CONTENT_TYPE = "application/json";
    private final String ENCODING = "utf-8";

    private static final String PLATFORM_AMMOUNT = "***!@#!@#&*%jmanhelmirjuujasd89172!@#$$%%Aams0";

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
     *
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
        if (!TextUtils.isEmpty(response)) {
            statusResponse = jsonToBean(response, GetLoginStatusResponse.class);
        }

        return statusResponse;
    }

    /**
     * 用户补充资料
     *
     * @param name
     * @return
     */
    public CompleteInfoResponse completeInfo(String loginName, String syncName, String name) throws HttpException {
        String url = String.format(BASE_URL + "/user/register/%s", loginName);
        RLog.v("CompleteInfoResponse", url);
        StringEntity entity = null;
        try {
            entity = new StringEntity(BeanTojson(new CompleteInfoRequest(name, syncName)), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String response = httpManager.post(mContext, url, entity, CONTENT_TYPE);
        RLog.v("CompleteInfoResponse", response);
        CompleteInfoResponse infoResponse = null;
        if (!TextUtils.isEmpty(response)) {
            infoResponse = jsonToBean(response, CompleteInfoResponse.class);
        }

        return infoResponse;
    }

    /**
     * 搜索联系人
     *
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
        if (!TextUtils.isEmpty(response)) {
            searchResponse = jsonToBean(response, SearchContactResponse.class);
        }

        return searchResponse;
    }


    /**
     * 批量搜索搜索联系人
     *
     * @param sycnName
     * @return
     * @throws HttpException
     */
    public SearchContactListResponse searchContact(List<String> sycnName) throws HttpException {
        String url = String.format(BASE_URL + "/user/batch", sycnName);
        RLog.v("searchContact", url);
        StringEntity entity = null;
        try {
            entity = new StringEntity(BeanTojson(sycnName), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HashMap<String,String> maps = new HashMap<>();
        maps.put("syncname",BeanTojson(sycnName));
        String response = httpManager.post(mContext,url,entity,CONTENT_TYPE);
        RLog.v("searchContact", response);
        SearchContactListResponse searchResponse = null;
        if (!TextUtils.isEmpty(response)) {
            searchResponse = jsonToBean(response, SearchContactListResponse.class);
        }
        return searchResponse;
    }

    /**
     * 添加好友邀请
     *
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
        if (!TextUtils.isEmpty(response)) {
            getResponse = jsonToBean(response, getAddFriendResponse.class);
        }

        return getResponse;
    }


    /**
     * 添加好友邀请
     *
     * @param user
     * @param friend
     * @return
     * @throws HttpException
     */
    public FriendResponse userDetail(String user, String friend) throws HttpException {
        String url = String.format(BASE_URL + "/user/details?friendname=%s&username=%s", friend, user);
        RLog.v("FriendResponse", url);
        String response = httpManager.get(url);
        RLog.v("FriendResponse", response);
        FriendResponse getResponse = null;
        if (!TextUtils.isEmpty(response)) {
            getResponse = jsonToBean(response, FriendResponse.class);
        }

        return getResponse;
    }

    /**
     * 获取申请好友的列表
     *
     * @param syncName
     * @param time
     * @return
     * @throws HttpException
     */
    public GetRelationFriendResponse getRaletionFriend(String syncName, long time) throws HttpException {
        String url = String.format(BASE_URL + "/friend/request/list?pageSize=50&startTime=%d&username=%s", time, syncName);
        RLog.v("getRaletionFriend", url);
        String responseStr = httpManager.get(url);
        RLog.v("getRaletionFriend", responseStr);
        GetRelationFriendResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetRelationFriendResponse.class);
        }

        return response;
    }

    /**
     * 获取好友列表
     *
     * @param syncName
     * @param time
     * @return
     * @throws HttpException
     */
    public GetFriendResponse getFriends(String syncName, long time) throws HttpException {
        String url = String.format(BASE_URL + "/friend/list?pageSize=50&startTime=%d&username=%s", time, syncName);
        RLog.v("getFriends", url);
        String responseStr = httpManager.get(url);
        RLog.v("getFriends", responseStr);
        GetFriendResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetFriendResponse.class);
        }

        return response;
    }

    /**
     * 同意好友请求
     *
     * @param user
     * @param friend
     * @return
     * @throws HttpException
     */
    public AgreeFriendResponse agreeFriend(String user, String friend) throws HttpException {
        String url = String.format(BASE_URL + "/friend/accept?friendname=%s&username=%s", friend, user);
        RLog.v("agreeFriend", url);
        String responseStr = httpManager.get(url);
        RLog.v("agreeFriend", responseStr);
        AgreeFriendResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, AgreeFriendResponse.class);
        }

        return response;

    }

    /**
     * 修改昵称
     *
     * @param syncName
     * @param newName
     * @return
     * @throws HttpException
     */
    public ModifyNameResponse modifyName(String syncName, String newName) throws HttpException {
        String url = String.format(BASE_URL + "/user/nickname/%s?nickname=%s", syncName, newName);
        RLog.v("modifyName", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyName", responseStr);
        ModifyNameResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, ModifyNameResponse.class);
        }

        return response;
    }

    /**
     * 朋友圈
     *
     * @param syncName
     * @param time
     * @param pageSize
     * @return
     * @throws HttpException
     */
    public GetCircleResponse getCircle(String syncName, long time, int pageSize) throws HttpException {
        String url = String.format(BASE_URL + "/circle/list?pageSize=%d&startTime=%d&username=%s", pageSize, time, syncName);
        RLog.v("getCircle", url);
        String responseStr = httpManager.get(url);
        RLog.v("getCircle", responseStr);
        GetCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetCircleResponse.class);
        }

        return response;
    }

    public GetCircleResponse getCircleByUserName(String assignedName,String syncName, long time, int pageSize) throws HttpException {
        String url = String.format(BASE_URL + "/circle/assigned/list?assignedName=%s&pageSize=%d&startTime=%d&username=%s", assignedName,pageSize, time, syncName);
        RLog.v("getCircle", url);
        String responseStr = httpManager.get(url);
        RLog.v("getCircle", responseStr);
        GetCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetCircleResponse.class);
        }

        return response;
    }


    /**
     * 朋友圈
     *
     * @param syncName
     * @param time
     * @param pageSize
     * @return
     * @throws HttpException
     */
    public GetCircleResponse getCollected(String syncName, long time, int pageSize) throws HttpException {
        String url = String.format(BASE_URL + "/circle/collect/list?pageSize=%d&startTime=%d&username=%s", pageSize, time, syncName);
        RLog.v("getCircle", url);
        String responseStr = httpManager.get(url);
        RLog.v("getCircle", responseStr);
        GetCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetCircleResponse.class);
        }

        return response;
    }

    /**
     * 我的金额
     *
     * @param syncName
     * @return
     * @throws HttpException
     */
    public GetMineAmountResponse getAmount(String syncName) throws HttpException {
        String url = String.format(BASE_URL + "/account/balance?username=%s", syncName);
        RLog.v("getAmount", url);
        String responseStr = httpManager.get(url);
        RLog.v("getAmount", responseStr);
        GetMineAmountResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetMineAmountResponse.class);
        }

        return response;
    }

    /**
     * 转账
     *
     * @param targetSyncname
     * @param money
     * @param syncName
     * @return
     * @throws HttpException
     */
    public TransferResponse transfer(String targetSyncname, double money, String syncName) throws HttpException {
        String url = String.format(BASE_URL + "/account/transfer?acceptor=%s&balance=%f&username=%s", targetSyncname, money, syncName);
        RLog.v("transfer", url);
        String responseStr = httpManager.get(url);
        RLog.v("transfer", responseStr);
        TransferResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, TransferResponse.class);
        }

        return response;
    }

    /**
     * 获取七牛上传token
     *
     * @param type
     * @param syncName
     * @return
     * @throws HttpException
     */
    public GetQiNiuTokenResponse getQiNiuToken(int type, String syncName) throws HttpException {
        String url = null;
        if (type == GetQiNiuTokenResponse.PORTRAIT_TYPE) {
            url = String.format(BASE_URL + "/qiniu/accessToken?tokenType=%s&username=%s", "PORTRAIT", syncName);
        } else {
            url = String.format(BASE_URL + "/qiniu/accessToken?tokenType=%s&username=%s", "CIRCLE", syncName);
        }

        RLog.v("GetQiNiuTokenResponse", url);
        String responseStr = httpManager.post(url);
        GetQiNiuTokenResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetQiNiuTokenResponse.class);
        }

        return response;
    }

    /**
     * 修改头像
     *
     * @param syncName
     * @param imageUrl
     * @return
     * @throws HttpException
     */
    public ModifyPortraitResponse modifyPortrait(String syncName, String imageUrl) throws HttpException {
        String url = String.format(BASE_URL + "/user/qiniu/portrait/%s?imagePath=%s", syncName, imageUrl);
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        ModifyPortraitResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, ModifyPortraitResponse.class);
        }

        return response;
    }

    /**
     * 发布朋友圈
     *
     * @param content
     * @param syncName
     * @param urlList
     * @return
     * @throws HttpException
     */
    public PublishCircleResponse publishCircle(String content, String syncName, List<String> urlList) throws HttpException {
        String url = String.format(BASE_URL + "/circle/qiniu/publish?content=%s&username=%s", content, syncName);
        RLog.v("publishCircle", url);
        StringEntity entity = null;
        try {
            entity = new StringEntity(BeanTojson(urlList), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String responseStr = httpManager.post(mContext, url, entity, CONTENT_TYPE);
        RLog.v("publishCircle", responseStr);
        PublishCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, PublishCircleResponse.class);
        }

        return response;
    }

    /**
     * 平台余额
     *
     * @param syncName
     * @return
     * @throws HttpException
     */
    public GetPlatformAmmountResponse getPlatformAmmount(String syncName) throws HttpException {
        String url = String.format(BASE_URL + "/account/cloud/balance?username=%s", syncName);

        RLog.v("getPlatformAmmount", url);
        String responseStr = httpManager.post(url);
        RLog.v("getPlatformAmmount", responseStr);
        GetPlatformAmmountResponse response = null;
        if (!TextUtils.isEmpty(
                responseStr)) {
            response = jsonToBean(responseStr, GetPlatformAmmountResponse.class);
        }

        return response;
    }

    /**
     * 充值轮询结果
     *
     * @param recharge
     * @param transId
     * @param loginName
     * @return
     * @throws HttpException
     */
    public GetRechargeStatusResponse getRechargeStatus(double recharge, String transId, String loginName) throws HttpException {
        String url = String.format(BASE_URL + "/password/check?amount=%f&transId=%s&username=%s", recharge, transId, loginName);
        RLog.v("getRechargeStatus", url);
        String responseStr = httpManager.post(url);
        RLog.v("getRechargeStatus", responseStr);
        GetRechargeStatusResponse response = null;
        if (!TextUtils.isEmpty(
                responseStr)) {
            response = jsonToBean(responseStr, GetRechargeStatusResponse.class);
        }

        return response;
    }

    /**
     * 删除好友
     *
     * @param targetSync
     * @param syncName
     * @return
     * @throws HttpException
     */
    public DeleteContactResponse deleteContact(String targetSync, String syncName) throws HttpException {
        String url = String.format(BASE_URL + "/friend/relieve?friendname=%s&username=%s", targetSync, syncName);
        RLog.v("deleteContact", url);
        String responseStr = httpManager.get(url);
        RLog.v("deleteContact", responseStr);
        DeleteContactResponse response = null;
        if (!TextUtils.isEmpty(
                responseStr)) {
            response = jsonToBean(responseStr, DeleteContactResponse.class);
        }

        return response;
    }

    /**
     * 转账历史
     *
     * @param syncName
     * @param requestTime
     * @return
     * @throws HttpException
     */
    public GetTransferHistoryResponse getTransferHistory(String syncName, long requestTime) throws HttpException {
        String url = String.format(BASE_URL + "/redpackage/accept?pageSize=%d&startTime=%d&username=%s", 20, requestTime, syncName);
        RLog.v("getTransferHistory", url);
        String responseStr = httpManager.get(url);
        RLog.v("getTransferHistory", responseStr);
        GetTransferHistoryResponse response = null;
        if (!TextUtils.isEmpty(
                responseStr)) {
            response = jsonToBean(responseStr, GetTransferHistoryResponse.class);
        }

        return response;
    }

    /**
     * 转账统计
     *
     * @param syncName
     * @return
     * @throws HttpException
     */
    public GetTransferAggregationResponse getTransferAggregation(String syncName) throws HttpException {
        String url = String.format(BASE_URL + "/redpackage/aggregation?username=%s", syncName);
        RLog.v("getTransferAggregation", url);
        String responseStr = httpManager.get(url);
        RLog.v("getTransferAggregation", responseStr);
        GetTransferAggregationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetTransferAggregationResponse.class);
        }

        return response;
    }

    /**
     * 删除朋友圈
     *
     * @param syncName
     * @param id
     * @return
     * @throws HttpException
     */
    public DeleteSelfCircleResponse deleteSelfCircle(String syncName, long id) throws HttpException {
        String url = String.format(BASE_URL + "/circle/delete?circle=%d&username=%s", id, syncName);
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.post(url);
        RLog.v("deleteSelfCircle", responseStr);
        DeleteSelfCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteSelfCircleResponse.class);
        }

        return response;
    }


    //点赞朋友圈
    public DeleteSelfCircleResponse likeCircle(String syncName, long id) throws HttpException {
        String url = String.format(BASE_URL + "/circle/like?circle=%d&username=%s", id, syncName);
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.post(url);
        RLog.v("deleteSelfCircle", responseStr);
        DeleteSelfCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteSelfCircleResponse.class);
        }

        return response;
    }


    //收藏朋友圈
    public DeleteSelfCircleResponse collectCircle(String syncName, long id) throws HttpException {
        String url = String.format(BASE_URL + "/circle/collect?circle=%d&username=%s", id, syncName);
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.post(url);
        RLog.v("deleteSelfCircle", responseStr);
        DeleteSelfCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteSelfCircleResponse.class);
        }

        return response;
    }

    public DeleteSelfCircleResponse cancleCollectCircle(String syncName, long id) throws HttpException {
        String url = String.format(BASE_URL + "/circle/collect/cancel?circle=%d&username=%s", id, syncName);
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.post(url);
        RLog.v("deleteSelfCircle", responseStr);
        DeleteSelfCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteSelfCircleResponse.class);
        }

        return response;
    }

    //投诉朋友圈
    public DeleteSelfCircleResponse complainCircle(String syncName, long id) throws HttpException {
        String url = String.format(BASE_URL + "/circle/complaint?circle=%d&username=%s", id, syncName);
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.post(url);
        RLog.v("deleteSelfCircle", responseStr);
        DeleteSelfCircleResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteSelfCircleResponse.class);
        }

        return response;
    }


    //获取广告
    public AdResponse getLoadingAd() throws HttpException {
        String url = String.format(BASE_URL + "/splashScreen/latest");
        RLog.v("deleteSelfCircle", url);
        String responseStr = httpManager.get(url);
        RLog.v("deleteSelfCircle", responseStr);
        AdResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, AdResponse.class);
        }

        return response;
    }

    /**
     * 客服列表
     *
     * @param pageSize
     * @param requestTime
     * @return
     * @throws HttpException
     */
    public GetCustomerListResponse getCustomerList(int pageSize, long requestTime) throws HttpException {
        String url = String.format(BASE_URL + "/customer/list?pageSize=%d&startTime=%d", pageSize, requestTime);
        RLog.v("getCustomerList", url);
        String responseStr = httpManager.get(url);
        RLog.v("getCustomerList", responseStr);
        GetCustomerListResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GetCustomerListResponse.class);
        }


        return response;
    }


    /**
     * 最新的应用版本
     *
     * @return
     * @throws HttpException
     */
    public UpdateVersionResponse appLatest() throws HttpException {
        String url = String.format(BASE_URL + "/app/latest?platform=%s", "ANDROID");
        RLog.v("applatest", url);
        String responseStr = httpManager.get(url);
        RLog.v("applatest", responseStr);
        UpdateVersionResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, UpdateVersionResponse.class);
        }

        return response;
    }


    /**
     * 用户修改好友的备注名称
     *
     * @param friendId 好友Id
     * @throws HttpException
     */
    public SetFriendDisplayNameResponse friendRemark(String friendId, String remarkName, String userName) throws HttpException {

        String url = String.format(BASE_URL + "/friend/remark?friendname=%s&remarkName=%s&username=%s", friendId, remarkName, userName);
        RLog.e("ziji", url);
        String result = httpManager.get(mContext, url);
        RLog.e("ziji", result);
        SetFriendDisplayNameResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, SetFriendDisplayNameResponse.class);
        }
        return response;
    }



    /**
     * 直接创建群  默认只有 自己
     *
     * @param name
     * @param icon
     * @param brief
     * @return
     * @throws HttpException
     */
    public CreateGroupBaoResponse createGroup(String username, String name, String icon, String brief) throws HttpException {
        String url = BASE_URL+"/group/create?username=" + username;
        JSONObject groupinf = new JSONObject();
        try {
            /**
             *  "createTime1": 0,
             "groupIcon": "string",
             "groupIntro": "string",
             "groupMemberCount": 0,
             "groupName": "string",
             "groupToken": "string",
             "id": 0
             */

            groupinf.put("groupIcon", icon);
            groupinf.put("groupIntro", brief);
            groupinf.put("groupName", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringEntity entity = null;
        try {
            entity = new StringEntity(groupinf.toString(), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = httpManager.post(mContext, url, entity, CONTENT_TYPE);
        CreateGroupBaoResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, CreateGroupBaoResponse.class);
        }
        return response;
    }


    /**
     * 获取群列表
     * @param userName
     * @return
     * @throws HttpException
     */
    public GroupListBaoResponse getGroupList(String userName) throws HttpException {

        String url = String.format(BASE_URL + "/group/list?pageSize=%s&startTime=%s&username=%s", "30", "0", userName);
        RLog.e("ziji", url);
        String result = httpManager.get(mContext, url);
        RLog.e("ziji", result);
        GroupListBaoResponse response = null;
        if (!TextUtils.isEmpty(result)) {
            response = jsonToBean(result, GroupListBaoResponse.class);
        }
        return response;
    }



    /**
     * 修改群头像
     *
     * @param syncName
     * @param imageUrl
     * @return
     * @throws HttpException
     */
    public ModifyPortraitResponse modifyGroupIcon(String token,String syncName, String imageUrl) throws HttpException {
        String url = String.format(BASE_URL + "/group/modify/icon?groupIcon=%s&token=%s&username=%s",imageUrl, token ,syncName );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        ModifyPortraitResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, ModifyPortraitResponse.class);
        }

        return response;
    }


    /**
     * 获取群组信息  /group/details/token
     * @param
     * @return
     */
    public Object getGroupInfo(String groupToken,String username)throws HttpException  {
        String url = String.format(BASE_URL + "/group/details/token?groupToken=%s&username=%s",groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.get(url);
        RLog.v("modifyPortrait", responseStr);
        GroupDetailBaoResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GroupDetailBaoResponse.class);
        }

        return response;
    }


    /**
     * 解散群
     * @param groupToken
     * @param username
     * @return
     * @throws HttpException
     */
    public Object groupDissmiss(String groupToken,String username)throws HttpException  {
        String url = String.format(BASE_URL + "/group/dismiss/token?token=%s&username=%s",groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        GroupDetailBaoResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GroupDetailBaoResponse.class);
        }

        return response;
    }


    /**
     * 群成员退出群组
     * @param groupToken
     * @param username
     * @return
     * @throws HttpException
     */
    public Object groupQuit(String groupToken,String username)throws HttpException  {
        String url = String.format(BASE_URL + "/group/member/quit/token?token=%s&username=%s",groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        QuitGroupResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, QuitGroupResponse.class);
        }

        return response;
    }

    /**
     * 获取群员列表
     * @param groupToken
     * @param username
     * @return
     * @throws HttpException
     */
    public Object getGroupNumbers(String groupToken,String username)throws HttpException  {
        String url = String.format(BASE_URL + "/group/member/list/token?pageSize=%s&startTime=0&token=%s&username=%s",String.valueOf(Integer.MAX_VALUE),groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.get(url);
        RLog.v("modifyPortrait", responseStr);
        GroupNumbersBaoResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, GroupNumbersBaoResponse.class);
        }

        return response;
    }

    /**
     * 设置群名称
     * @param newGroupName
     * @param groupToken
     * @param username
     * @return
     * @throws HttpException
     */
    public Object setGroupName(String  newGroupName,String groupToken, String username) throws HttpException {
        String url = String.format(BASE_URL + "/group/modify/groupName?groupName=%s&token=%s&username=%s",newGroupName,groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        QuitGroupResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, QuitGroupResponse.class);
        }

        return response;

    }


    /**
     * 添加群成员
     * @param groupToken
     * @param username
     * @param startDisList
     * @return
     */
    public Object addGroupMember(String groupToken,String username, List<String> startDisList)throws HttpException  {
        String url = String.format(BASE_URL + "/group/invitation/batch/request/token?token=%s&username=%s",groupToken, username );

        StringEntity entity = null;
        try {
            entity = new StringEntity(BeanTojson(startDisList), ENCODING);
            entity.setContentType(CONTENT_TYPE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HashMap<String,String> maps = new HashMap<>();
        maps.put("friendname",BeanTojson(startDisList));

        String responseStr = httpManager.post(mContext, url, entity, CONTENT_TYPE);
        RLog.v("modifyPortrait", responseStr);
        AddGroupMemberResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, AddGroupMemberResponse.class);
        }

        return response;
    }

    //group/member/kickout?member=sdf&token=dfs&username=sdf
    public Object deleGroupMember(String groupToken,String username,String kickoutName) throws HttpException {

        String url = String.format(BASE_URL + "/group/member/kickout?member=%s&token=%s&username=%s",kickoutName,groupToken, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        DeleteGroupMemberResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, DeleteGroupMemberResponse.class);
        }
        return response;
    }

    /**
     * 申请加入群成员
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object sendFriendInvitation(String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/join/request/token?token=%s&username=%s",token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 接受用户申请入群
     * @param memberName
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object acceptetFriendInvitation(String memberName,String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/request/accept/token?memberName=%s&token=%s&username=%s",memberName,token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 拒绝用户申请入群
     * @param memberName
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object refuseFriendInvitation(String memberName,String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/request/reject/token?memberName=%s&token=%s&username=%s",memberName,token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 用户接受入群邀请
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */

    public Object invitationFriendInvitation(String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/invitation/accept/token?token=%s&username=%s",token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 用户拒绝入群邀请
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object invitaitonRefuseFriendInvitation(String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/invitation/reject/token?token=%s&username=%s",token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }


    /**
     * 更新群公告
     * @param info
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object updateGroupNotice(String info,String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/modify/groupIntro?groupIntro=%s&token=%s&username=%s",info,token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }




    /**
     * 群升级
     * @param token
     * @param username
     * @return
     * @throws HttpException
     */
    public Object groupUpdate(String token, String username) throws HttpException{
        String url = String.format(BASE_URL + "/group/upgrade?token=%s&username=%s",token, username );
        RLog.v("modifyPortrait", url);
        String responseStr = httpManager.post(url);
        RLog.v("modifyPortrait", responseStr);
        FriendInvitationResponse response = null;
        if (!TextUtils.isEmpty(responseStr)) {
            response = jsonToBean(responseStr, FriendInvitationResponse.class);
        }
        return response;
    }
}
