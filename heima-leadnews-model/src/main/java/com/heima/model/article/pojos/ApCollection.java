package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ap_collection")
public class ApCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("entry_id")
    private Integer entryId;

    /**
     * 文章id
     */
    @TableField("article_id")
    private Long articleId;

    /**
     *  点赞内容类型
     *      0文章
     *      1动态
     */
    @TableField("type")
    private Short type;

    /**
     * 创建时间
     */
    @TableField("collection_time")
    private Date collectionTime;

    /**
     * 发布时间
     */
    @TableField("published_time")
    private Date publishedTime;
}
