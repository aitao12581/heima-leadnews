package com.heima.model.behaviior.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnLikesBehaviorDto implements Serializable {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 0 不喜欢      1 取消不喜欢
     */
    private Short type;
}
