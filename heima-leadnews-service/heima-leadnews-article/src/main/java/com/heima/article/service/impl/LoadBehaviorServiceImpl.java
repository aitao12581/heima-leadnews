package com.heima.article.service.impl;

import com.heima.article.service.LoadBehaviorService;
import com.heima.common.constants.BehaviorConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleInfoDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class LoadBehaviorServiceImpl implements LoadBehaviorService {

    @Autowired
    private CacheService cacheService;

    /**
     * 文章用户行为回显
     *
     * @param dto 文章Id 作者id
     * @return
     */
    @Override
    public ResponseResult loadBehavior(ArticleInfoDto dto) {
        // 判断参数有效性
        if (dto == null && dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断用户登录状态
        ApUser user = AppThreadLocalUtil.getUser();
        if (user==null) {
            return  ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 查询redis库中的数据
        String key = BehaviorConstants.BEHAVIOR_INFO+user.getId()+"_"+dto.getArticleId();

        boolean like = cacheService.hExists(key, "like");
        boolean unlike = cacheService.hExists(key, "unlike");
        boolean collection = cacheService.hExists(key, "collection");
        boolean follow = cacheService.hExists(key, "follow");

        Map<String, Boolean> map = new HashMap<>();
        map.put("islike", like);
        map.put("isunlike", unlike);
        map.put("iscollection", collection);
        map.put("isfollow", follow);

        return ResponseResult.okResult(map);
    }
}
