package com.heima.model.article.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 收藏行为数据模型
 */
@Data
public class CollectionBehaviorDto implements Serializable {

    /**
     * 文章id
     */
    private Long entryId;

    /**
     *  0收藏    1取消收藏
     */
    private Short operation;

    /**
     * 发布时间
     */
    private Date publishedTime;

    /**
     * 文章类型
     *      0 文章
     *      1 动态
     */
    private Short type;
}
