package com.heima.model.article.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleInfoDto implements Serializable {

    /**
     * 文章Id
     */
    private Long articleId;

    /**
     * 作者id
     */
    private Integer authorId;
}
