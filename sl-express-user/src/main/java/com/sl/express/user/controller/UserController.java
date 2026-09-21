package com.sl.express.user.controller;

import com.sl.express.common.result.Result;
import com.sl.express.user.dto.LoginRequest;
import com.sl.express.user.dto.RegisterRequest;
import com.sl.express.user.service.UserService;
import com.sl.express.user.vo.LoginVO;
import com.sl.express.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(userService.getUser(userId));
    }
}
