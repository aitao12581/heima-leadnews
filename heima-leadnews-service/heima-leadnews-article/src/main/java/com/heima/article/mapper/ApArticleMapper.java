package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    /**
     * 分页查询文章数据
     * @param dto   条件模型
     * @param loadType 加载模式
     * @return
     */
    List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDto dto,@Param("type") Short loadType);

    /**
     * 获取文章数据在时间范围内
     * @param dayParam
     * @return
     */
    List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dayParam);

    /**
     * 数量不为null 在原基础上加1
     * @param entryId
     * @param type
     */
    void updateOfType(@Param("id") Long entryId,@Param("type") String type);

    /**
     * 对数据进行减一操作
     * @param entryId
     * @param type
     */
    void subOftype(@Param("id") Long entryId,@Param("type") String type);
}
