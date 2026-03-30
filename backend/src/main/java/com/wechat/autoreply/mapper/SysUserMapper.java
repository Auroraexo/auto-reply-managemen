package com.wechat.autoreply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wechat.autoreply.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
