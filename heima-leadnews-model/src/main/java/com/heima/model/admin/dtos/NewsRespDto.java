package com.heima.model.admin.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class NewsRespDto implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 自媒体用户ID
     */
    private Integer userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 图文内容
     */
    private String content;

    /**
     * 文章布局
     0 无图文章
     1 单图文章
     3 多图文章
     */
    private Short type;

    /**
     * 图文频道ID
     */
    private Integer channelId;

    private String labels;

    /**
     * 创建时间
     */
    private Long createdTime;

    /**
     * 提交时间
     */
    private Long submitedTime;

    /**
     * 当前状态
     0 草稿
     1 提交（待审核）
     2 审核失败
     3 人工审核
     4 人工审核通过
     8 审核通过（待发布）
     9 已发布
     */
    private Short status;

    /**
     * 定时发布时间，不定时则为空
     */
    private Long publishTime;

    /**
     * 拒绝理由
     */
    private String reason;

    /**
     * 发布库文章ID
     */
    private Long articleId;

    /**
     * //图片用逗号分隔
     */
    private String images;

    /**
     * 状态
     *  0 下架
     *  1 上架
     */
    private Short enable;

    /**
     * 作者名称
     */
    private String authorName;
}
