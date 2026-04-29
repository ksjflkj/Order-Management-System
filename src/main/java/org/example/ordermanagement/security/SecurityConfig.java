package org.example.ordermanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 全局安全配置类
 * <p>
 * 负责配置跨域资源共享（CORS）、CSRF 防护、会话管理策略、接口访问权限（路由白名单与角色限制）等安全设定。
 * 同时注册了密码编码器（PasswordEncoder）、认证管理器（AuthenticationManager），
 * 并配置了自定义的 JWT 过滤器的执行顺序。
 * </p>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤链，定义各 HTTP 请求路径的鉴权规则及核心策略
     *
     * @param http HttpSecurity 配置对象
     * @return 配置完成的 SecurityFilterChain 实例
     * @throws Exception 配置时产生异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        // 智能客服：放行到 Controller 层做鉴权（避免 Security 层 403）
                        .requestMatchers("/api/chat/**").permitAll()
                        // 公开浏览接口：商家列表、详情、菜品、评价 —— 无需登录直接放行
                        .requestMatchers(HttpMethod.GET, "/api/user/merchant/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/dish/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/user/review/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/review/**").permitAll()
                        // 管理员专属
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 上传、商家后台：仅真实登录用户
                        .requestMatchers("/api/upload/**").hasAnyRole("USER", "ADMIN", "MERCHANT")
                        .requestMatchers("/api/merchant/**").hasAnyRole("USER", "ADMIN", "MERCHANT")
                        // 用户个人接口（购物车、订单、地址、设置等）：仅真实用户
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN", "MERCHANT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器配置，提供安全的密码散列哈希算法
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器，利用 Spring Security 提供的配置构建 AuthenticationManager
     *
     * @param configuration 认证配置对象
     * @return AuthenticationManager 实例
     * @throws Exception 获取时可能抛出的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 配置跨域资源共享 (CORS) 访问策略
     *
     * @return CorsConfigurationSource 实例，装配了所有允许跨域访问的方法、头部及源路径
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}