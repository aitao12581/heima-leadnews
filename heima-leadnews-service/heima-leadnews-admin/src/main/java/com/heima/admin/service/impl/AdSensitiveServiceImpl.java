package com.heima.admin.service.impl;

import com.heima.admin.service.AdSensitiveService;
import com.heima.apis.admin.IAdminClient;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.utils.common.ADThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class AdSensitiveServiceImpl implements AdSensitiveService {

    @Autowired
    private IAdminClient adminClient;

    /**
     * 敏感词管理 分页查询数据
     *
     * @param dto 查询条件
     */
    @Override
    public ResponseResult list(SensitiveDto dto) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.senList(dto);
    }

    /**
     * 保存敏感词
     *
     * @param sensitive
     * @return
     */
    @Override
    public ResponseResult save(WmSensitive sensitive) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.saveSen(sensitive);
    }

    /**
     * 删除敏感词
     *
     * @param id 敏感词id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.delSen(id);
    }

    /**
     * 修改敏感词信息
     *
     * @param sensitive 修改内容
     * @return
     */
    @Override
    public ResponseResult update(WmSensitive sensitive) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.updateData(sensitive);
    }
}
