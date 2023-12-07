package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户搜索记录历史
     *
     * @param keyword 关键字
     * @param userId  用户id
     */
    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        // 1.查询当前用户的搜索关键词
        Query query = Query.query(Criteria.where("userId").is(userId)
                .and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        // 存在 更新创建时间
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        // 不存在，判断当前历史记录总数量是否超过10
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        // 查询该用户所有的关键字
        Query query1 = Query.query(Criteria.where("userId").is(userId));
        query1.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> searchList = mongoTemplate.find(query1, ApUserSearch.class);

        if (searchList == null || searchList.size() < 10) {
            mongoTemplate.save(apUserSearch);
        } else {
            // 获取最后一条数据
            ApUserSearch lastSearch = searchList.get(searchList.size() - 1);

            // 替换最后一条数据
            mongoTemplate.findAndReplace(Query.query(Criteria.where("id")
                            .is(lastSearch.getId())), apUserSearch);
        }
    }

    /**
     * 查询搜索历史
     *
     * @return
     */
    @Override
    public ResponseResult findUserSearch() {
        // 获取当前用户
        ApUser user = AppThreadLocalUtil.getUser();

        System.out.println(user);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 根据用户查询数，按照时间倒序
        List<ApUserSearch> apUserSearches = mongoTemplate.find(Query.query(Criteria.where("userId")
                                .is(user.getId()))
                        .with(Sort.by(Sort.Direction.DESC, "createdTime")),
                ApUserSearch.class);

        return ResponseResult.okResult(apUserSearches);
    }

    /**
     * 删除搜索历史
     *
     * @param dto 搜索历史的id
     * @return
     */
    @Override
    public ResponseResult delUserSearch(HistorySearchDto dto) {
        // 检查参数
        if (dto.getId()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 在数据库中删除数据
        mongoTemplate.remove(Query.query(Criteria.where("userId").is(user.getId())
                .and("id").is(dto.getId())), ApUserSearch.class);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
