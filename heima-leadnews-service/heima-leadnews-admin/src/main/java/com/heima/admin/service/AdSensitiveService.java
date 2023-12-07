package com.heima.admin.service;

import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmSensitive;

/**
 * 后台管理  敏感词管理
 */
public interface AdSensitiveService {

    /**
     * 敏感词管理
     * @param dto 查询条件
     */
    ResponseResult list(SensitiveDto dto);

    /**
     * 保存敏感词
     * @param sensitive 敏感词
     * @return
     */
    ResponseResult save(WmSensitive sensitive);

    /**
     * 删除敏感词
     * @param id    敏感词id
     * @return
     */
    ResponseResult del(Integer id);

    /**
     * 修改敏感词信息
     * @param sensitive 修改内容
     * @return
     */
    ResponseResult update(WmSensitive sensitive);
}
