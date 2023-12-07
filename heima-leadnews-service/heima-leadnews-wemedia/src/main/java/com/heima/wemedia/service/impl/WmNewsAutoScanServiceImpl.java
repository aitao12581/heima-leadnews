package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;

    /**
     * 自媒体文章审核
     *
     * @param id 文章id
     */
    @Async  // 标明当前方法是一个异步方法
    @Override
    public void autoScanWmNews(Integer id) {
        // 判断参数
        if (id == null) {
            throw new RuntimeException("无效参数：id");
        }

        // 判断自媒体文章是否存在
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews==null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-文章不存在");
        }

        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            // 自媒体文章处于待审核状态
            // 从文章中提取纯文本内容和图片地址
            Map<String, Object> textAndImages =  handleTextOrImage(wmNews);

            // 自管理敏感词过滤
            boolean isSensitive = handleSensitiveScan(textAndImages.get("content").toString(), wmNews);
            if (!isSensitive) {
                updateWmnews(wmNews, (short) 2, "文字内容包含敏感词");
                return;
            }

            // 审核文本内容 阿里云接口
            boolean isTextScan = handleTextScan(textAndImages.get("content").toString(), wmNews);
            if (!isTextScan) return;

            // 审核图片内容 阿里云接口
            boolean isImageScan = handleImageScan((List<String>)
                    textAndImages.get("image"), wmNews);
            if (!isImageScan) {
                updateWmnews(wmNews, (short) 2, "图片内容包含敏感词");
                return;
            }

            // 审核完成，保存app端相关的文章数据
            ResponseResult result = saveAppArticle(wmNews);
//            System.out.println("服务降级后返回的状态码："+result.getCode());
            // 判断是否保存成功
            if (!result.getCode().equals(200)) {
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }

            // 保存成功，回填article_id
            wmNews.setArticleId((Long) result.getData());
            updateWmnews(wmNews, (short) 9, "审核成功");
        }
    }

    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 自管理敏感词审核
     * @param content   文章内容
     * @param wmNews    自媒体文章信息
     * @return 过滤结果
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag = true;

        // 判断参数有效性
        if (content == null || wmNews == null) {
            return false;
        }

        // 获取所有敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery()
                .select(WmSensitive::getSensitives));
        List<String> collect = wmSensitives.stream()
                .map(WmSensitive::getSensitives).collect(Collectors.toList());

        // 初始化敏感词库
        SensitiveWordUtil.initMap(collect);

        // 查看文章是否包含敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if (map.size() > 0) {
            updateWmnews(wmNews, (short) 2, "当前文章中存在违规内容"+map);
            flag = false;
        }
        return flag;
    }

    @Autowired
    private IArticleClient articleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     *  保存app端相关的文章数据
     * @param wmNews 自媒体文章数据
     * @return 修改状态
     */
    private ResponseResult saveAppArticle(WmNews wmNews) {
        // 创建容器存储数据
        ArticleDto dto = new ArticleDto();

        // 将自媒体文章信息复制到dto中
        BeanUtils.copyProperties(wmNews, dto);

        // 文件布局
        dto.setLayout(wmNews.getType());

        // 频道
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel!=null) {
            dto.setChannelName(wmChannel.getName());
        }

        // 作者
        dto.setAuthorId(wmNews.getUserId().longValue());
        WmUser user = wmUserMapper.selectById(wmNews.getUserId());
        if (user!=null) {
            dto.setAuthorName(user.getName());
        }

        // 设置文章id
        if (wmNews.getArticleId() != null) {
            dto.setId(wmNews.getArticleId());
        }
        // 重新定义创建时间
        dto.setCreatedTime(new Date());

        ResponseResult result = articleClient.saveArticle(dto);
        return result;
    }

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private Tess4jClient tess4jClient;

    /**
     * 使用阿里云接口实现对图片内容进行审核
     * @param images 图片数据
     * @param wmNews 文章数据
     * @return 审核结果
     */
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        // 定义标记
        boolean flag = true;

        // 判断参数有效性
        if (images==null || images.size() == 0 ) {
            return flag;
        }

        // 从minIO容器中下载图片
        // 图片去重
        images = images.stream().distinct().collect(Collectors.toList());

        // 定义容器存储图片二进制数据
        List<byte[]> imageList = new ArrayList<>();

        try {
            // 下载图片，并将二进制数据存入集合中
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                // 图片文字审核
                // 从byte[] 转换为butteredImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage imageFile = ImageIO.read(in);
                // 识别图片的文字
                String result = tess4jClient.doOCR(imageFile);

                System.out.println("图片结果："+result);
                // 审核文字内容是否包含敏感词
                boolean isTextScan = handleSensitiveScan(result, wmNews);
                System.out.println(isTextScan);
                if (!isTextScan) return isTextScan;

                imageList.add(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // 调用接口审核图片内容
//        try {
//            Map maps = greenImageScan.imageScan(imageList);
//            if (maps!=null) {
//                // 审核失败
//                if (maps.get("suggestion").equals("block")) {
//                    flag = false;
//                    updateWmnews(wmNews, (short) 2, "当前文章中存在违规内容");
//                }
//
//                // 需要人工审核
//                if (maps.get("suggestion").equals("review")) {
//                    flag = false;
//                    updateWmnews(wmNews, (short) 3, "当前文章存在不确定内容");
//                }
//            }
//        } catch (Exception e) {
//            flag = false;
//            log.info("审核过程失败：", e);
//        }
        return flag;
    }

    @Autowired
    private GreenTextScan greenTextScan;

    /**
     * 调用阿里云接口对文本内容进行审核
     * @param content   文本内容
     * @param wmNews    自媒体文章对象
     * @return 审核结果
     */
    private boolean handleTextScan(String content, WmNews wmNews) {
        // 定义标记
        boolean flag = true;

        // 如果自媒体没有上传名称以及文章内容，直接审核通过
        if ((wmNews.getTitle()+"-"+content).length() == 1) {
            return flag;
        }

        // 存在内容调用阿里云接口进行审核
//        try {
//            Map map = greenTextScan.greeTextScan(wmNews.getTitle() + "-" + content);
//            if (map!=null) {
//                // 成功调用接口，对返回结果进行判断
//                if (map.get("suggestion").equals("block")) {
//                    // 审核失败
//                    flag = false;
//                    updateWmnews(wmNews, (short) 2, "当前文章中存在违规内容");
//                }
//
//                // 不确定信息  需要人工审核
//                if (map.get("suggestion").equals("review")) {
//                    flag = false;
//                    updateWmnews(wmNews, (short) 3, "当前文章存在不确定内容");
//                }
//            }
//        } catch (Exception e) {
//            flag = false;
//            log.info("审核过程失败：", e);
//        }
        return flag;
    }

    /**
     * 修改自媒体文章 Wm-news表
     * @param wmNews    文章内容
     * @param status    文章状态
     * @param reason  失败信息
     */
    private void updateWmnews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 从自媒体文章中提取字体内容以及图片信息
     * 1。从自媒体文章的内容中提取文本和图片
     * 2.提取文章的封面图片
     * @param wmNews 文章信息
     * @return 分解出的内容
     */
    private Map<String, Object> handleTextOrImage(WmNews wmNews) {
        // 创建容器存储文本内容
        StringBuilder text = new StringBuilder();
        List<String> images = new ArrayList<>();

        // 从自媒体文章中提取文本和图片
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            // 将json字符串转换为json数据
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);

            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    text.append(map.get("value"));
                }

                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }
        // 提取文章的封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] urls = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(urls));
        }

        // 将结果封装到map集合中返回
        Map<String, Object> result = new HashMap<>();
        result.put("content", text);
        result.put("image", images);
        return result;
    }
}
