package cn.rongcloud.im.server.response;

import java.util.List;

/**
*
* Author: 邓言诚  Create at : 2018/6/14  19:52
* Email: yanchengdeng@gmail.com
* Describle: 查找联系人列表信息
*/
public class SearchContactListResponse {

    private int code;
    private String message;
    private List<SearchContactResponse.ResultEntity> data;

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

    public List<SearchContactResponse.ResultEntity> getData() {
        return data;
    }

    public void setData(List<SearchContactResponse.ResultEntity> data) {
        this.data = data;
    }


}
