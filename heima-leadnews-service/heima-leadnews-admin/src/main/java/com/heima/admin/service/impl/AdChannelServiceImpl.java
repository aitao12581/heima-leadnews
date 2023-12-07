package com.heima.admin.service.impl;

import com.heima.admin.service.AdChannelService;
import com.heima.apis.admin.IAdminClient;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.utils.common.ADThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class AdChannelServiceImpl implements AdChannelService {

    @Autowired
    private IAdminClient adminClient;

    /**
     * 分页查询频道数据
     *
     * @param dto 条件
     * @return
     */
    @Override
    public ResponseResult list(ChannelDto dto) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.list(dto);
    }

    /**
     * 新增频道信息
     *
     * @param wmChannel 频道信息
     * @return
     */
    @Override
    public ResponseResult save(WmChannel wmChannel) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.save(wmChannel);
    }

    /**
     * 修改频道信息
     *
     * @param wmChannel 修改后的内容
     * @return
     */
    @Override
    public ResponseResult update(WmChannel wmChannel) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.update(wmChannel);
    }

    /**
     * 删除频道信息
     *
     * @param id 要删除的频道id
     * @return
     */
    @Override
    public ResponseResult del(Integer id) {
        AdUser user = ADThreadLocalUtil.getUser();
        if (user==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        return adminClient.del(id);
    }
}
