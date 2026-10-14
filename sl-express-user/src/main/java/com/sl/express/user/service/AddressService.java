package com.sl.express.user.service;

import com.sl.express.user.dto.AddressRequest;
import com.sl.express.user.vo.AddressVO;

import java.util.List;

public interface AddressService {
    List<AddressVO> list(Long userId);
    AddressVO create(Long userId, AddressRequest request);
    AddressVO update(Long userId, Long addressId, AddressRequest request);
    void delete(Long userId, Long addressId);
    AddressVO setDefault(Long userId, Long addressId);
}
