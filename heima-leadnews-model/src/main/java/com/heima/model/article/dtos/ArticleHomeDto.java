package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

/**
 * 用于接收前端返回的查询条件
 */

@Data
public class ArticleHomeDto {

    // 最大时间
    Date maxBehotTime;
    // 最小时间
    Date minBehotTime;
    // 分页size
    Integer size;
    // 频道ID
    String tag;
}
