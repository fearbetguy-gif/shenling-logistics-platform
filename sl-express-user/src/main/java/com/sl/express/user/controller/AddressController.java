package com.sl.express.user.controller;

import com.sl.express.common.result.Result;
import com.sl.express.user.dto.AddressRequest;
import com.sl.express.user.service.AddressService;
import com.sl.express.user.vo.AddressVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public Result<List<AddressVO>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(addressService.list(userId));
    }

    @PostMapping
    public Result<AddressVO> create(@RequestHeader("X-User-Id") Long userId,
                                    @Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.create(userId, request));
    }

    @PutMapping("/{addressId}")
    public Result<AddressVO> update(@RequestHeader("X-User-Id") Long userId,
                                    @PathVariable Long addressId,
                                    @Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.update(userId, addressId, request));
    }

    @DeleteMapping("/{addressId}")
    public Result<Void> delete(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long addressId) {
        addressService.delete(userId, addressId);
        return Result.success();
    }

    @PutMapping("/{addressId}/default")
    public Result<AddressVO> setDefault(@RequestHeader("X-User-Id") Long userId,
                                         @PathVariable Long addressId) {
        return Result.success(addressService.setDefault(userId, addressId));
    }
}
