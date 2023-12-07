package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialService extends IService<WmMaterial> {


    /**
     * 图片上传
     * @param multipartFile 图片接收对象
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile);


    /**
     * 查询素材列表
     * @param dto 分析查询条件
     */
    ResponseResult findList(WmMaterialDto dto);
}
