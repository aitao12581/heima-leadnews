package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.admin.dtos.NewsRespDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.NewsEnableDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    /**
     * 查询所有频道数据
     *
     * @param dto 查询条件
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {
        // 条件判空
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 检查参数
        dto.checkParam();

        // 获取登录人信息
        WmUser user = WmThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 分页条件查询
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        // 筛选状态
        if (dto.getStatus()!=null) {
            lqw.eq(WmNews::getStatus, dto.getStatus());
        }

        // 筛选频道
        if (dto.getChannelId() != null) {
            lqw.eq(WmNews::getChannelId, dto.getChannelId());
        }

        // 发布时间范围查询
        if (dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null) {
            lqw.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }

        // 关键字模糊查询
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            lqw.like(WmNews::getTitle, dto.getKeyword());
        }

        // 绑定当前登录的用户
        lqw.eq(WmNews::getUserId, user.getId());

        // 根据发布时间进行倒序排序
        lqw.orderByDesc(WmNews::getPublishTime);

        page = page(page, lqw);

        // 结果返回
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    /**
     * 发布文章或保存草稿
     *
     * @param dto 修改后的内容
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        // 条件判空
        if (dto == null || dto.getContent() == null) {
            ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 保存或修改文章

        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);

        // 封面图片 将list转换为string
        if (dto.getImages()!=null && dto.getImages().size() > 0) {
            // 将集合中的链接地址转换为字符串，以，隔开
            String images = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(images);
        }

        // 当封面类型为-1 则类型修改为默认
        if (WemediaConstants.WM_NEWS_TYPE_AUTO.equals(dto.getType())) {
            wmNews.setType(null);
        }

        // 保存或修改数据
        saveOrUpdateWmNews(wmNews);

        // 判断是否为草稿，是草稿则结束该方法
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 3.不是草稿，保存文章封面图片与素材的关系
        // 获取文章内容中的图片信息
        List<String> materials = ectractUrlInfo(dto.getContent());

        // 将素材和文章的关系映射存入表中
        saveRelativeInfoForContent(materials,wmNews.getId());

        // 不是草稿，保存文章封面图片和素材的关系，如果当前布局为自动，需要匹配封面图片
        saveRelativeInfoForCover(dto, wmNews, materials);

        // 异步调用自动审核方法
//        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        wmNewsTaskService.addNewsToTask(wmNews.getId(), wmNews.getPublishTime());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 第一个功能：如果当前封面类型为自动，则设置封面类型的数据
     * 匹配规则：
     * 1，如果内容图片大于等于1，小于3  单图  type 1
     * 2，如果内容图片大于等于3  多图  type 3
     * 3，如果内容没有图片，无图  type 0
     *
     * 第二个功能：保存封面图片与素材的关系
     * @param dto 修改或保存的数据
     * @param wmNews    保存的文章信息
     * @param materials 图片素材信息
     */
    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials) {
        List<String> images = dto.getImages();

        // 如果封面类型为自动，则设置封面类型数据
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            // 多图
            if (materials.size() > 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            } else if (materials.size()>=1 && materials.size()<3) {
                // 单图
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            } else {
                // 无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            // 修改文章
            if (images == null && images.size() == 0) {
                wmNews.setImages(StringUtils.join(images, ","));
            }
            updateById(wmNews);
        }

        if (images!=null && images.size()>0) {
            saveRelativeInfo(images, wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
    }

    /**
     * 处理文章内容图片与素材的关系
     * @param materials 素材信息
     * @param id 文章Id
     */
    private void saveRelativeInfoForContent(List<String> materials, Integer id) {
        saveRelativeInfo(materials, id, WemediaConstants.WM_CONTENT_REFERENCE);
    }

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    /**
     * 保存文章图片与素材的关系到数据库中
     * @param materials 图片信息
     * @param id    文章id
     * @param type  引用类型
     */
    private void saveRelativeInfo(List<String> materials, Integer id, Short type) {
        // 判断素材信息是否为空
        if (materials != null && !materials.isEmpty()) {
            // 通过图片url查询素材的id
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(
                    Wrappers.<WmMaterial>lambdaQuery()
                            .in(WmMaterial::getUrl, materials));
            // 判断素材是否有效
            if (wmMaterials==null || wmMaterials.size()==0) {
                // 抛出异常使调用者发现，方便回滚事务
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            // 提交素材与查询出的素材数量不一致 抛出异常
            if (materials.size() != wmMaterials.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
            }

            List<Integer> ids = wmMaterials.stream()
                    .map(WmMaterial::getId).collect(Collectors.toList());

            // 批量保存
            wmNewsMaterialMapper.saveRelations(ids, id, type);

        }
    }


    /**
     * 获取文章内容中的图片信息
     * @param content 文章内容
     * @return
     */
    private List<String> ectractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();

        // 将文章内容信息转换为java对象并进行遍历
        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String imgUrl = (String) map.get("value");
                materials.add(imgUrl);
            }
        }

        return materials;
    }


    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        // 补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);    // 默认上架

        // id为空则为新增
        if (wmNews.getId() == null) {
            save(wmNews);
        } else {
            // 修改
            // 删除文章图片与素材的关联
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                    .eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }

    /**
     * 删除文章
     *
     * @param newId 文章的id
     * @return
     */
    @Override
    public ResponseResult delNews(Integer newId) {
        // 对id进行判空操作
        if (newId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断是否在登录状态
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 获取当前id下的数据
        WmNews wmNews = getOne(Wrappers.<WmNews>lambdaQuery()
                .eq(WmNews::getId, newId)
                .eq(WmNews::getUserId, user.getId()));

        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 判断要删除的文章类型
        if (!wmNews.getStatus().equals(WemediaConstants.WM_SAVE_STATUS)) {
            // 其他文章类型需要删除三张关联表
            // 获取中间表中所有的素材编号
            List<Integer> materialids = getmaterialIds(newId);
            // 删除子表数据（素材库）
            if (materialids.size()>0 && materialids!=null) {
                wmMaterialMapper.delete(Wrappers.<WmMaterial>lambdaQuery()
                        .eq(WmMaterial::getUserId, user.getId())
                        .in(WmMaterial::getId, materialids));

                // 删除中间表数据
                wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                        .eq(WmNewsMaterial::getNewsId, newId));
            }
        }

        // 草稿，只需要在wmnews表删除数据即可
        remove(Wrappers.<WmNews>lambdaQuery()
                .eq(WmNews::getId, newId)
                .eq(WmNews::getUserId, user.getId()));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 获取子表相关文章的id
     * @param newId 文章id
     * @return
     */
    private List<Integer> getmaterialIds(Integer newId) {
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(Wrappers
                .<WmNewsMaterial>lambdaQuery()
                .eq(WmNewsMaterial::getNewsId, newId));

        List<Integer> ids = wmNewsMaterials.stream()
                .map(WmNewsMaterial::getMaterialId)
                .collect(Collectors.toList());
        return ids;
    }

    /**
     * 获取当前id下的文章数据
     *
     * @param newId 文章id
     * @return
     */
    @Override
    public ResponseResult getOne(Integer newId) {
        // 判断参数有效性
        if (newId==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 判断当前状态
        WmUser user = WmThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 根据文章id查询该文章的信息
        WmNews wmNews = getOne(Wrappers.<WmNews>lambdaQuery()
                .eq(WmNews::getUserId, user.getId())
                .eq(WmNews::getId, newId));

        return ResponseResult.okResult(wmNews);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 根据自媒体文章id 修改后的状态 控制文章上下架
     *
     * @param dto 数据模型 id 和 状态
     * @return 是否成功
     */
    @Override
    public ResponseResult downOrUp(NewsEnableDto dto) {
        // 检查参数
        if (dto.getId()==null && dto.getEnable()==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        if (dto.getEnable()<=-1 || dto.getEnable()>=2) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 查询文章
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        // 判断文章是否发布状态
        if (!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID
                    , "当前文章不是发布状态，无法上下架");
        }

        // 修改文章
        update(Wrappers.<WmNews>lambdaUpdate()
                .set(WmNews::getEnable, dto.getEnable())
                .eq(WmNews::getId, wmNews.getId()));

        if (wmNews.getArticleId()!=null) {
            Map<String, Object> map = new HashMap<>();
            map.put("articleId", wmNews.getArticleId());
            map.put("enable", dto.getEnable());
            kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC,
                    JSON.toJSONString(map));
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    @Autowired
    private WmUserService wmUserService;
    /**
     * 为后台管理提供自媒体文章信息
     *
     * @param dto 条件
     * @return
     */
    @Override
    public ResponseResult listVo(NewsAuthDto dto) {
        // 判断参数有效性
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 检查参数
        dto.checkParam();

        // 创建分页器
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());

        // 添加条件
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dto.getStatus()!=null, WmNews::getStatus, dto.getStatus());
        if (dto.getTitle()!=null && dto.getTitle()!="") {
            lqw.like(WmNews::getTitle, dto.getTitle());
        }
        lqw.orderByDesc(WmNews::getCreatedTime);

        page = page(page, lqw);

        // 封装结果
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        List<WmNews> records = page.getRecords();
        List<NewsRespDto> list = records.stream().map(item -> {
            NewsRespDto resp = new NewsRespDto();
            BeanUtils.copyProperties(item, resp);
            resp.setCreatedTime(item.getCreatedTime().getTime());
            resp.setSubmitedTime(item.getSubmitedTime().getTime());
            resp.setPublishTime(item.getPublishTime().getTime());
            // 根据userid查询作者姓名
            WmUser user = wmUserService.getById(item.getUserId());
            resp.setAuthorName(user.getName());
            return resp;
        }).collect(Collectors.toList());
        result.setData(list);
        return result;
    }

    /**
     * 查询单个用户数据
     *
     * @param id 文章id
     * @return
     */
    @Override
    public ResponseResult oneVo(Integer id) {
        // 判断参数
        if (id == null || id < 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(id);
        if (wmNews==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 封装结果
        NewsRespDto dto = new NewsRespDto();
        BeanUtils.copyProperties(wmNews, dto);

        // 获取作者信息
        WmUser user = wmUserService.getById(wmNews.getUserId());

        // 封装结果
        dto.setCreatedTime(wmNews.getCreatedTime().getTime());
        dto.setSubmitedTime(wmNews.getSubmitedTime().getTime());
        dto.setPublishTime(wmNews.getPublishTime().getTime());

        dto.setAuthorName(user.getName());

        return ResponseResult.okResult(dto);
    }

    /**
     * 人工审核失败
     *
     * @param dto id  状态  原因
     * @return
     */
    @Override
    public ResponseResult authFail(NewsAuthDto dto) {
        // 检查参数
        if (dto == null || dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 获取当前id的文章
        WmNews wmNews = getById(dto.getId());
        wmNews.setStatus((short) 2);
        wmNews.setReason(dto.getMsg());

        // 修改表数据
        updateById(wmNews);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 人工审核成功
     *
     * @param dto id 状态
     * @return
     */
    @Override
    public ResponseResult authPass(NewsAuthDto dto) {
        // 检查参数
        if (dto == null || dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 获取当前id的文章
        WmNews wmNews = getById(dto.getId());
        wmNews.setStatus((short) 4);
        wmNews.setReason("人工审核成功");

        updateById(wmNews);
        // 修改表
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
