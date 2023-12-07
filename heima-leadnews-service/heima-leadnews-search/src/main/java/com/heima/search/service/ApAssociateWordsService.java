package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

/**
 * <p>
 * 联想词表 服务类
 * </p>
 *
 * @author itheima
 */
public interface ApAssociateWordsService {

    /**
     * 联想词
     * @param dto 条件
     * @return  响应数据
     */
    ResponseResult findAssociate(UserSearchDto dto);
}
