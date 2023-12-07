package com.heima.model.behaviior.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class LikesBehaviorDto implements Serializable {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 0 点赞
     * 1 取消点赞
     */
    private Short operation;

    /**
     * 点赞内容类型
     *  0文章  1动态   2评论
     */
    private Short type;
}
