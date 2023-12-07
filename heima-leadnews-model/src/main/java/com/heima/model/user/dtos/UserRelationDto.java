package com.heima.model.user.dtos;

import lombok.Data;

/**
 * 文章关注
 */
@Data
public class UserRelationDto {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 作者id
     */
    private Integer authorId;

    /**
     * 关注状态
     */
    private short operation;
}
