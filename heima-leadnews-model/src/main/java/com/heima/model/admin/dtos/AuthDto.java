package com.heima.model.admin.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class AuthDto extends PageRequestDto {

    /**
     * 编号
     */
    private Integer id;

    /**
     * 理由
     */
    private String msg;

    /**
     * 状态
     */
    private Integer status;
}
