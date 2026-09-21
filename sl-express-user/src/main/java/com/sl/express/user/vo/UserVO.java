package com.sl.express.user.vo;

import com.sl.express.user.entity.User;

public record UserVO(Long id, String phone, String nickname) {
    public static UserVO from(User user) {
        return new UserVO(user.getId(), user.getPhone(), user.getNickname());
    }
}
