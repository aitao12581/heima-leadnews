package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.SensitiveDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmSensitive;

public interface WmSensitiveService extends IService<WmSensitive> {
    /**
     * 分页 查询所有敏感词
     * @param dto 条件
     * @return
     */
    ResponseResult findList(SensitiveDto dto);

    /**
     * 保存数据
     * @param sensitive 保存的内容
     * @return
     */
    ResponseResult saveSen(WmSensitive sensitive);

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    ResponseResult del(Integer id);

    /**
     * 修改敏感词
     * @param sensitive 敏感词信息
     * @return
     */
    ResponseResult updateData(WmSensitive sensitive);
}
