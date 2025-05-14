package com.nowcoder.community.service;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yl
 * @date 2025-04-22 13:11
 */
public interface UserService{
    Map<String, Object> register(User user);

    int activation(int id, String code);

    User findUserById(int userId);

    LoginTicket findLoginTicket(String ticket);

    Map<String, Object> login(String username, String password, int expiredSeconds);

    int updateHeader(int id, String header_url);

    void logout(String ticket);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
