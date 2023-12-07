package com.heima.behavior.service;

import com.heima.model.behaviior.dtos.LikesBehaviorDto;
import com.heima.model.behaviior.dtos.ReadBehaviorDto;
import com.heima.model.behaviior.dtos.UnLikesBehaviorDto;
import com.heima.model.common.dtos.ResponseResult;

public interface BehaviorService {

    /**
     * 用户为文章点赞
     * @param dto   文章的信息
     * @return
     */
    ResponseResult likesBehavior(LikesBehaviorDto dto);

    /**
     * 用户阅读文章次数
     * @param dto
     * @return
     */
    ResponseResult readBehavior(ReadBehaviorDto dto);

    /**
     * 文章喜欢 或取消喜欢
     * @param dto   信息
     * @return
     */
    ResponseResult unLikesBehavior(UnLikesBehaviorDto dto);
}
