package com.sl.express.user.vo;

import com.sl.express.user.entity.Address;

public record AddressVO(Long id, String contactName, String contactPhone,
                        String province, String city, String district,
                        String detailAddress, Integer isDefault) {
    public static AddressVO from(Address address) {
        return new AddressVO(address.getId(), address.getContactName(), address.getContactPhone(),
                address.getProvince(), address.getCity(), address.getDistrict(),
                address.getDetailAddress(), address.getIsDefault());
    }
}
