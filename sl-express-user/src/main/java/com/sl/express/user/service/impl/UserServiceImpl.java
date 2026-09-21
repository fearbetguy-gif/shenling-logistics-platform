package com.sl.express.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sl.express.common.exception.BusinessException;
import com.sl.express.user.dto.LoginRequest;
import com.sl.express.user.dto.RegisterRequest;
import com.sl.express.user.entity.User;
import com.sl.express.user.mapper.UserMapper;
import com.sl.express.user.service.JwtTokenService;
import com.sl.express.user.service.UserService;
import com.sl.express.user.vo.LoginVO;
import com.sl.express.user.vo.UserVO;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    @Transactional
    public UserVO register(RegisterRequest request) {
        if (userMapper.findByPhone(request.phone()) != null) {
            throw new BusinessException("手机号已注册");
        }
        User user = new User();
        user.setPhone(request.phone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return UserVO.from(user);
    }

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.findByPhone(request.phone());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("用户已被禁用");
        }
        return new LoginVO(jwtTokenService.createToken(user.getId()), UserVO.from(user));
    }

    @Override
    public UserVO getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.from(user);
    }
}
