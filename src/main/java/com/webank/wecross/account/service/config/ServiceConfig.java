package com.webank.wecross.account.service.config;

import com.webank.wecross.account.service.account.UADetailsService;
import com.webank.wecross.account.service.account.UAManager;
import com.webank.wecross.account.service.authentication.JwtAuthenticationFilter;
import com.webank.wecross.account.service.authentication.JwtLoginFilter;
import com.webank.wecross.account.service.authentication.JwtManager;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class ServiceConfig extends WebSecurityConfigurerAdapter {
    private static Logger logger = LoggerFactory.getLogger(ServiceConfig.class);

    @Resource UADetailsService uaDetailsService;
    @Resource JwtManager jwtManager;
    @Resource UAManager uaManager;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(uaDetailsService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(
                        "/auth/register",
                        "/auth/pub",
                        "/auth/authCode"); // TODO: use one configure in cors()
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JwtLoginFilter jwtLoginFilter =
                new JwtLoginFilter(authenticationManager(), jwtManager, uaManager);
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(authenticationManager(), jwtManager, uaManager);

        http.cors()
                .and()
                .cors()
                .and()
                .csrf()
                .disable() // TODO: enable this
                .authorizeRequests()
                .antMatchers("/auth/register**")
                .permitAll()

                // TODO: check role
                // .anyRequest()
                // .authenticated()

                .and()
                .formLogin()
                .loginProcessingUrl(jwtLoginFilter.getLoginPath())
                .and()
                .addFilter(jwtLoginFilter)
                .addFilter(jwtAuthenticationFilter)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
