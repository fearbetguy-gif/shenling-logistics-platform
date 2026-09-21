package com.sl.express.user.service;

import com.sl.express.user.dto.LoginRequest;
import com.sl.express.user.dto.RegisterRequest;
import com.sl.express.user.vo.LoginVO;
import com.sl.express.user.vo.UserVO;

public interface UserService {
    UserVO register(RegisterRequest request);
    LoginVO login(LoginRequest request);
    UserVO getUser(Long userId);
}
