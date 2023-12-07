package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *  APP用户关注信息表
 */
@Data
@TableName("ap_user_follow")
public class ApUserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 关注的作者id
     */
    @TableField("follow_id")
    private Integer followId;

    /**
     * 作者昵称
     */
    @TableField("follow_name")
    private String followName;

    /**
     * 关注度
     *      0  偶尔感兴趣
     *      1  一般
     *      2  经常
     *      3  高度
     */
    @TableField("level")
    private Short level;

    /**
     *  是否动态通知
     */
    @TableField("is_notice")
    private Short isNotice;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;
}
