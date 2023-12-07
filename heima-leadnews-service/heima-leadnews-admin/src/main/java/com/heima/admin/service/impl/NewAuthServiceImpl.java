package com.heima.admin.service.impl;

import com.heima.admin.service.NewAuthService;
import com.heima.apis.admin.IAdminClient;
import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NewAuthServiceImpl implements NewAuthService {

    @Autowired
    private IAdminClient adminClient;

    /**
     * 查询所有文章信息
     * @param dto 条件
     * @return
     */
    @Override
    public ResponseResult listVo(NewsAuthDto dto) {
        return adminClient.listVo(dto);
    }

    /**
     * 查看单个文章详情
     *
     * @param id 文章id
     * @return
     */
    @Override
    public ResponseResult oneVo(Integer id) {
        return adminClient.oneVo(id);
    }

    /**
     * 人工审核成功
     *
     * @param dto id 和 状态
     * @return
     */
    @Override
    public ResponseResult authFail(NewsAuthDto dto) {
        return adminClient.authFail(dto);
    }

    /**
     * 人工审核失败
     *
     * @param dto id 和 状态
     * @return
     */
    @Override
    public ResponseResult authPass(NewsAuthDto dto) {
        return adminClient.authPass(dto);
    }
}
