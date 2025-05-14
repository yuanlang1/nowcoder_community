package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.PrintWriter;

/**
 * @author yl
 * @date 2025-04-29 21:21
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/resources/**");
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .addFilterBefore(ticketFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/user/setting",
                                "/user/upload",
                                "/comment/add/**",
                                "/letter/**",
                                "/notice/**",
                                "/like",
                                "/follow",
                                "/unfollow"
                        )
                        .hasAnyAuthority(
                                AUTHORITY_ADMIN,
                                AUTHORITY_USER,
                                AUTHORITY_MODERATOR
                        )
                        .requestMatchers(
                                "/discuss/top",
                                "/discuss/wonderful"
                        )
                        .hasAnyAuthority(
                                AUTHORITY_MODERATOR
                        )
                        .requestMatchers(
                                "/discuss/delete",
                                "/data/**",
                                "/actuator/**"
                        )
                        .hasAnyAuthority(
                                AUTHORITY_ADMIN
                        )
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                // 没有权限的异常处理
                .exceptionHandling(ex -> ex
                        // 权限不足处理
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String xRequestWith = request.getHeader("x-requested-with");
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null) {
                                auth.getAuthorities().forEach(a ->
                                        logger.info("AccessDeniedHandler - 当前用户角色: {}", a.getAuthority())
                                );
                            }
                            if ("XMLHttpRequest".equals(xRequestWith)) {
                                response.setContentType("application/plain;charset=utf-8");
                                PrintWriter writer = response.getWriter();
                                writer.write(CommunityUtil.getJSONString(403, "您无权限访问此功能"));
                            } else {
                                System.out.println("access = " + 403);
                                response.sendRedirect(request.getContextPath() + "/denied");
                            }
                        })
                        // 没有登陆处理
                        .authenticationEntryPoint((request, response, authException) -> {
                            String xRequestWith = request.getHeader("x-requested-with");
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth != null) {
                                auth.getAuthorities().forEach(a ->
                                        logger.info("AuthenticationEntryPoint - 当前用户角色: {}", a.getAuthority())
                                );
                            } else {
                                logger.info("AuthenticationEntryPoint - 未检测到 Authentication，用户未登录");
                            }
                            if ("XMLHttpRequest".equals(xRequestWith)) {
                                response.setContentType("application/plain;charset=utf-8");
                                PrintWriter writer = response.getWriter();
                                writer.write(CommunityUtil.getJSONString(403, "你还没有登录，请登录"));
                            } else {
                                System.out.println("auto = " + 403);
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        }))
                // 默认Logout退出
                .logout(logout -> logout.logoutUrl("/securityLogout"));
        return http.build();
    }
}
