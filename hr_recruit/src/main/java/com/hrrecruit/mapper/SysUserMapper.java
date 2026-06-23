package com.hrrecruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrrecruit.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    List<SysUser> selectUserWithRoles(@Param("username") String username);

    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    SysUser selectByUsernameIgnoreDeleted(@Param("username") String username);
}