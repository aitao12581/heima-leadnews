package com.heima.model.common.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class LoginDto implements Serializable {

    @ApiModelProperty(value = "手机号", required = true)
    private String phone;

    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
