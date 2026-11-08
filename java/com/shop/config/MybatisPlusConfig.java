package com.shop.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 插件配置。
 * 这里只装了分页插件：有了它，Service 里 page(new Page<>(1,12), wrapper) 才会
 * 自动拼上 limit，并额外查一次 count。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最多 100 条，防止前端传个 99999 把库拖死
        pagination.setMaxLimit(100L);
        interceptor.addInnerInterceptor(pagination);

        return interceptor;
    }
}
