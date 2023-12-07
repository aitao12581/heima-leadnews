package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.common.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 图片上传
     *
     * @param multipartFile 图片接收对象
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        // 判断参数是否为空
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 上传图片到minio中
        String fileName = UUID.randomUUID().toString().replace("-", "");

        // 获取上传文件的名称
        String originalFilename = multipartFile.getOriginalFilename();
        // 截取文件格式
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));

        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName+postfix, multipartFile.getInputStream());
            log.info("上传文件到MinIO中，fileId：{}", fileId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("WmMaterialServiceImpl-上传文件失败");
        }

        // 将数据保存到数据库中
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short)0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);

        // 返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 查询素材列表
     *
     * @param dto 分析查询条件
     */
    @Override
    public ResponseResult findList(WmMaterialDto dto) {

        // 检查参数
        dto.checkParam();

        // 分页查询
        Page<WmMaterial> pageInfo = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmMaterial> lqw = new LambdaQueryWrapper<>();
        lqw.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());

        // 判断收藏状态是否存在 状态为收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1) {
            lqw.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }

        // 将数据根据创建时间倒序排序
        lqw.orderByDesc(WmMaterial::getCreatedTime);

        pageInfo = page(pageInfo, lqw);

        // 返回结果
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                (int) pageInfo.getTotal());
        result.setData(pageInfo.getRecords());
        return result;
    }
}
