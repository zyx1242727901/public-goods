package com.xh.publicgoods.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhangyuxiao
 */
@Configuration
public class PublicGoodsWebMvcConfig implements WebMvcConfigurer {
    private final String globalPrefix = "/api/goods";

    /**
     * 添加统一前缀
     * @param configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer){
        configurer.addPathPrefix(globalPrefix, HandlerTypePredicate.forAnnotation(RestController.class));
        configurer.addPathPrefix(globalPrefix, HandlerTypePredicate.forAnnotation(Controller.class));
    }
}
