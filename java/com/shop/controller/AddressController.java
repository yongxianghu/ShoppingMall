package com.shop.controller;

import com.shop.common.Result;
import com.shop.entity.AddressTbl;
import com.shop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址。userId 由前端带上（教学版没有 Token）。
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<AddressTbl>> list(@RequestParam Integer userId) {
        return Result.ok(addressService.list(userId));
    }

    @PostMapping
    public Result<AddressTbl> add(@RequestParam Integer userId, @RequestBody AddressTbl address) {
        return Result.ok("添加成功", addressService.add(userId, address));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@RequestParam Integer userId,
                               @PathVariable Integer id,
                               @RequestBody AddressTbl address) {
        address.setId(id);
        addressService.update(userId, address);
        return Result.ok("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam Integer userId, @PathVariable Integer id) {
        addressService.delete(userId, id);
        return Result.ok("删除成功", null);
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@RequestParam Integer userId, @PathVariable Integer id) {
        addressService.setDefault(userId, id);
        return Result.ok("已设为默认地址", null);
    }
}
