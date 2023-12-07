package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper, AdUser>
implements AdUserService
{

    /**
     * 登录后台管理
     *
     * @param dto 用户登录的账户密码
     * @return
     */
    @Override
    public ResponseResult login(AdUserDto dto) {
        System.out.println(dto);
        // 在参数有效的情况下执行
        if (StringUtils.isNotBlank(dto.getName()) && StringUtils.isNotBlank(dto.getPassword())) {
            // 根据name获取数据库中的该name的信息
            AdUser user = getOne(Wrappers.<AdUser>lambdaQuery()
                    .eq(AdUser::getName, dto.getName()));

            if (user==null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }

            // 比较密码是否正确
            String salt = user.getSalt();
            String password = dto.getPassword();
            String pwd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
            if (!pwd.equals(user.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            // 账户和密码校验成功包装要返回的数据
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(user.getId().longValue()));
            user.setSalt("");
            user.setPassword("");
            map.put("user", user);

            log.info("用户登录成功");
            return ResponseResult.okResult(map);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }
}
