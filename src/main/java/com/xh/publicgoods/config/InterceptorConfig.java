package com.xh.publicgoods.config;

import com.xh.publicgoods.interceptor.PublicGoodsRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by wuchenxu on 2020/5/27.
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

  @Bean
  public PublicGoodsRequestInterceptor publicGoodsRequestInterceptor() {
    return new PublicGoodsRequestInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 拦截器需要拦截的路径
    String[] pathPatterns = {"/**"};
    registry.addInterceptor(publicGoodsRequestInterceptor()).addPathPatterns(pathPatterns);
  }

}
