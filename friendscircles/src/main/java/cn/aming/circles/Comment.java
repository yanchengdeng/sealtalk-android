package cn.aming.circles;

/**
 * Created by AMing on 16/12/7.
 * Company RongCloud
 */

public class Comment {
    /**
     * 评论内容
     */
    private String content;
    /**
     * 被评论人id
     */
    private String id;
    /**
     * 被评论人的name
     */
    private String name;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Comment(String content, String id, String name) {
        this.content = content;
        this.id = id;
        this.name = name;
    }
}
