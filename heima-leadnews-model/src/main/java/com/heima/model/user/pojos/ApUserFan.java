package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP用户粉丝信息表
 */
@Data
@TableName("ap_user_fan")
public class ApUserFan implements Serializable {

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
     * 粉丝id
     */
    @TableField("fans_id")
    private Integer fansId;

    /**
     * 粉丝昵称
     */
    @TableField("fans_name")
    private String fansName;

    /**
     *  粉丝忠诚度
     *   0 正常
     *   1 潜力股
     *   2 勇士
     *   3 铁杆
     *   4 老铁
     */
    @TableField("level")
    private Short level;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 是否可见我动态
     */
    @TableField("is_display")
    private Short isDisplay;

    /**
     * 是否屏蔽私信
     */
    @TableField("is_shield_letter")
    private Short isShieldLetter;

    /**
     * 是否屏蔽评论
     */
    @TableField("is_shield_comment")
    private Short isShieldComment;
}
