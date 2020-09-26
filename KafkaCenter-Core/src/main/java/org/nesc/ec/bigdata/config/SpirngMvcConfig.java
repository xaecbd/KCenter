package org.nesc.ec.bigdata.config;

import org.nesc.ec.bigdata.interceptor.SessionTimeOutInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Truman.P.Du
 * @date 2019年4月16日 上午9:48:16
 * @version 1.0
 */
@Configuration
public class SpirngMvcConfig implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		SessionTimeOutInterceptor sessionTimeOutInterceptor = new SessionTimeOutInterceptor();
		String[] allowUrls = {"/monitor/topic/consumer_offsets", "/monitor/topic/consumer_offsets/topic_metric",
				"/#/user/login", "/index.html",  "/login/user","/login/check","/config/oauth2","/api/consumer/status",
				"/favicon.png", "/lag", "/monitor/group/detail", "/monitor/alert/group","/manager/group/status","/home/topic/consumer/alert"};
		sessionTimeOutInterceptor.setAllowUrls(allowUrls);
		// 添加拦截器
		registry.addInterceptor(sessionTimeOutInterceptor).addPathPatterns("/**").
		   excludePathPatterns("/login/**","/user/verify","/static/**","/css/**","/js/**","/assets/**","/remote/**","/font/**");
	}
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");//
    }
}
