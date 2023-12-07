package com.heima.model.wemedia.dtos;

import lombok.Data;

/**
 * 接收前端返回的上下架状态模型数据
 */
@Data
public class NewsEnableDto {

    // 自媒体文章id
    private Integer id;
    /**
     * 是否上架  0 下架  1 上架
     */
    private Short enable;

}
