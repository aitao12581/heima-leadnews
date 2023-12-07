package com.heima.model.mess;

import lombok.Data;

/**
 * 对聚合之后的 分值 数据进行封装
 */
@Data
public class ArticleVisitStreamMess {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 阅读
     */
    private int view;

    /**
     * 收藏
     */
    private int collect;

    /**
     * 评论
     */
    private int commit;

    /**
     * 点赞
     */
    private int like;
}
