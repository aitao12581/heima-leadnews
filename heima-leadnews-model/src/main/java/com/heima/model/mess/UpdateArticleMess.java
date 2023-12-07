package com.heima.model.mess;

import lombok.Data;

/**
 * 消息发送封装类
 */
@Data
public class UpdateArticleMess {

    /**
     * 修改文章的字段类型
     */
    private UpdateArticleType type;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 修改修改数据的增量
     */
    private Integer add;

    public enum UpdateArticleType {
        COLLECTION,COMMENT,LIKES,VIEWS;
    }
}
