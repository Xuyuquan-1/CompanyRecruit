package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrrecruit.entity.SysNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {

    Long countUnreadByUserId(@Param("userId") Long userId);
}