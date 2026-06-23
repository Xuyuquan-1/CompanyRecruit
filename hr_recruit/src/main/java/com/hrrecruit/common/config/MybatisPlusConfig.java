package com.hrrecruit.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类 - 分页插件、逻辑删除插件配置
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件、逻辑删除插件、乐观锁插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 逻辑删除插件（可选，MyBatis-Plus 3.5.0+ 已内置支持@TableLogic注解）
        // 如果使用旧版本，需要添加：interceptor.addInnerInterceptor(new LogicDeleteInnerInterceptor());
        
        // 乐观锁插件（如果需要使用@Version注解）
        // interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }
}
