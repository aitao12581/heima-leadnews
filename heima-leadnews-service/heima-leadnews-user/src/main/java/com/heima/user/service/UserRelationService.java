package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRelationDto;

public interface UserRelationService {

    /**
     * 关注文章作者或取消关注
     * @param userRelationDto   信息
     * @return
     */
    ResponseResult userFollow(UserRelationDto userRelationDto);
}
