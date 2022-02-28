package org.nesc.ec.bigdata.security;

import org.nesc.ec.bigdata.config.AuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author rd87
 * @date 3/23/2019
 * @version 1.0
 */
@RestController
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthConfig authConfig;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf().disable()
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers("/", "/**", "/login", "/login/**", "/assets/**", "/webjars/**", "/error/**", "/js/**", "/css/**")
                .permitAll().anyRequest()
                .authenticated()
                .and().headers().frameOptions().disable().and().logout().logoutSuccessHandler(
                ((request, response, authentication) -> {
                    if(authentication != null) {
                        response.sendRedirect(authConfig.getOauthHost().substring(0,authConfig.getOauthHost().lastIndexOf("auth")) + "logout?redirect_uri=http://" +  request.getHeader("Host"));
                    } else{ response.sendRedirect("/");}
                })
        )
                //.permitAll().and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                ;
        // @formatter:on
    }
}
