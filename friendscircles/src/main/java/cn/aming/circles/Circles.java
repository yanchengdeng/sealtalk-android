package cn.aming.circles;

import java.util.List;

/**
 * Created by AMing on 16/12/7.
 * Company RongCloud
 */

public class Circles {
    /**
     * 发布的时间戳
     */
    protected long time;
    /**
     * 头像的 url
     */
    protected String portrait;
    /**
     * 昵称
     */
    protected String name;
    /**
     * 点赞的集合
     */
    protected List<Praise> praises;
    /**
     * 评论集合
     */
    protected List<Comment> Comments;
}
