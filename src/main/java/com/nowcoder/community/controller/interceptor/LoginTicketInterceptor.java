package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        System.out.println("ticket = " + ticket);
        System.out.println("ContextPath = " + request.getContextPath());
        if (ticket != null) {
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                // 根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                System.out.println("user = " + user);
                // 在本次请求中持有用户
                hostHolder.setUser(user);

                // 构建权限
                String type;
                List<GrantedAuthority> authorities = new ArrayList<>();
                switch (user.getType()){
                    case 1:
                        type = AUTHORITY_ADMIN;
                        break;
                    case 2:
                        type = AUTHORITY_MODERATOR;
                        break;
                    default:
                        type = AUTHORITY_USER;
                }
                authorities.add(new SimpleGrantedAuthority(type));
                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
                System.out.println("authentication = " + authentication);
                // 保存到SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 保存到session中
                HttpSession session = request.getSession(true);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext());

                return true;
            }
        }
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        System.out.println("LoginTicketInterceptor = " + "aaaa");
        SecurityContextHolder.clearContext();
    }
}
