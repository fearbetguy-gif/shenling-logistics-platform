package com.sl.express.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sl.express.common.exception.BusinessException;
import com.sl.express.user.dto.AddressRequest;
import com.sl.express.user.entity.Address;
import com.sl.express.user.mapper.AddressMapper;
import com.sl.express.user.service.AddressService;
import com.sl.express.user.vo.AddressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<AddressVO> list(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreatedAt))
                .stream().map(AddressVO::from).toList();
    }

    @Override
    @Transactional
    public AddressVO create(Long userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        fill(address, request);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        if (isDefault(request)) clearDefault(userId);
        addressMapper.insert(address);
        return AddressVO.from(address);
    }

    @Override
    @Transactional
    public AddressVO update(Long userId, Long addressId, AddressRequest request) {
        Address address = getOwnedAddress(userId, addressId);
        fill(address, request);
        address.setUpdatedAt(LocalDateTime.now());
        if (isDefault(request)) clearDefault(userId);
        addressMapper.updateById(address);
        return AddressVO.from(address);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = getOwnedAddress(userId, addressId);
        addressMapper.deleteById(address.getId());
    }

    @Override
    @Transactional
    public AddressVO setDefault(Long userId, Long addressId) {
        Address address = getOwnedAddress(userId, addressId);
        clearDefault(userId);
        address.setIsDefault(1);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);
        return AddressVO.from(address);
    }

    private Address getOwnedAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, addressId)
                .eq(Address::getUserId, userId));
        if (address == null) throw new BusinessException("地址不存在");
        return address;
    }

    private void clearDefault(Long userId) {
        Address update = new Address();
        update.setIsDefault(0);
        addressMapper.update(update, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }

    private void fill(Address address, AddressRequest request) {
        address.setContactName(request.contactName());
        address.setContactPhone(request.contactPhone());
        address.setProvince(request.province());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setDetailAddress(request.detailAddress());
        address.setIsDefault(isDefault(request) ? 1 : 0);
    }

    private boolean isDefault(AddressRequest request) {
        return Integer.valueOf(1).equals(request.isDefault());
    }
}
