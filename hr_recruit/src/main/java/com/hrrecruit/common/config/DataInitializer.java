package com.hrrecruit.common.config;

import com.hrrecruit.entity.SysUser;
import com.hrrecruit.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统初始化器
 * 启动时自动创建管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
    }

    private void initAdminUser() {
        SysUser existUser = sysUserMapper.selectByUsernameIgnoreDeleted("admin");

        if (existUser == null) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setPhone("13800000000");
            admin.setEmail("admin@hrrecruit.com");
            admin.setStatus(1);
            admin.setDeleted(0);

            sysUserMapper.insert(admin);
            log.info("======================================");
            log.info("管理员账号初始化完成！");
            log.info("用户名: admin");
            log.info("密码: 123456");
            log.info("======================================");
        } else {
            boolean needUpdate = false;

            if (existUser.getStatus() == null || existUser.getStatus() != 1) {
                existUser.setStatus(1);
                needUpdate = true;
            }
            if (existUser.getDeleted() == null || existUser.getDeleted() != 0) {
                existUser.setDeleted(0);
                needUpdate = true;
            }
            if (needUpdate) {
                sysUserMapper.updateById(existUser);
                log.info("管理员账号已修复（status={}, deleted={}）", existUser.getStatus(), existUser.getDeleted());
            } else {
                log.debug("管理员账号已存在，跳过初始化");
            }
        }
    }
}