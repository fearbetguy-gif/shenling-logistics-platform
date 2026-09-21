package com.sl.express.user.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sl.express.common.exception.BusinessException;
import com.sl.express.user.dto.RegisterRequest;
import com.sl.express.user.entity.User;
import com.sl.express.user.mapper.UserMapper;
import com.sl.express.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserServiceImplTest {

    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = org.mockito.Mockito.mock(UserMapper.class);
        userService = new UserServiceImpl(
                userMapper,
                new BCryptPasswordEncoder(),
                null
        );
    }

    @Test
    void shouldEncodePasswordBeforeInsert() {
        when(userMapper.findByPhone("13800138000")).thenReturn(null);

        userService.register(new RegisterRequest("13800138000", "123456", "张三"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertNotEquals("123456", captor.getValue().getPassword());
        assertNotEquals(null, captor.getValue().getCreatedAt());
    }

    @Test
    void shouldRejectDuplicatePhone() {
        User existing = new User();
        existing.setPhone("13800138000");
        when(userMapper.findByPhone("13800138000")).thenReturn(existing);

        assertThrows(BusinessException.class, () -> userService.register(
                new RegisterRequest("13800138000", "123456", "张三")));
    }
}
