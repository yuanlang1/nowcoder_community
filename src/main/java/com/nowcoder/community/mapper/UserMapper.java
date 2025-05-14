package com.nowcoder.community.mapper;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author yl
 * @date 2025-04-22 13:35
 */
@Mapper
public interface UserMapper {
    User selectByName(String username);

    User selectByEmail(String email);

    void insertUser(User user);

    User getById(int id);

    void updateStatus(int id, int status);

    int updateHeader(int id, String header_url);

    User getByName(String name);

    void readMessages(List<Integer> ids, int status);
}
