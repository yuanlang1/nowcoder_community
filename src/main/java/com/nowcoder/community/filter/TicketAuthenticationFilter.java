//package com.nowcoder.community.filter;
//
//import com.nowcoder.community.entity.LoginTicket;
//import com.nowcoder.community.entity.User;
//import com.nowcoder.community.service.UserService;
//import com.nowcoder.community.util.CommunityConstant;
//import com.nowcoder.community.util.CookieUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.checkerframework.checker.units.qual.A;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.context.SecurityContextRepository;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
///**
// * @author yl
// * @date 2025-04-29 22:34
// */
//@Component
//public class TicketAuthenticationFilter extends OncePerRequestFilter implements CommunityConstant {
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private SecurityContextRepository securityContextRepository;
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request){
//        String uri = request.getRequestURI();
//        return uri.startsWith("/register") || uri.startsWith("/kaptcha") || uri.startsWith("/login");
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String ticket = CookieUtil.getValue(request, "ticket");
//
//        if (ticket != null) {
//            LoginTicket loginTicket = userService.findLoginTicket(ticket);
//            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
//                User user = userService.findUserById(loginTicket.getUserId());
//                if (user != null) {
//                    List<GrantedAuthority> auths = new ArrayList<>();
//                    switch (user.getType()) {
//                        case 1:
//                            auths.add(new SimpleGrantedAuthority(AUTHORITY_ADMIN));
//                            break;
//                        case 2:
//                            auths.add(new SimpleGrantedAuthority(AUTHORITY_MODERATOR));
//                            break;
//                        default:
//                            auths.add(new SimpleGrantedAuthority(AUTHORITY_USER));
//                            break;
//                    }
//
//                    // 填充并持久化 SecurityContext
//                    UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(user, null, auths);
//                    SecurityContextHolder.getContext().setAuthentication(authToken);  // 填充上下文
//                    securityContextRepository.saveContext(
//                            SecurityContextHolder.getContext(), request, response
//                    );
//                }
//            }
//        }
//        filterChain.doFilter(request, response);
//    }
//}
