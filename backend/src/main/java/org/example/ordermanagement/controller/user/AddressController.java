package org.example.ordermanagement.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.result.Result;
import org.example.ordermanagement.dto.AddressSaveDTO;
import org.example.ordermanagement.service.AddressService;
import org.example.ordermanagement.vo.AddressVO;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户收货地址控制器 - 负责管理用户端填写的配送地址（列表查询、新增/修改、删除）
 */
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 新增或编辑收货地址
     * @param authentication 当前用户
     * @param dto 地址信息（包含经纬度、门牌号、联系人等，若带 id 则是修改）
     */
    @PostMapping("/save")
    public Result<String> save(Authentication authentication, @Valid @RequestBody AddressSaveDTO dto) {
        addressService.saveAddress(authentication.getName(), dto);
        return Result.successMessage("保存地址成功");
    }

    /**
     * 获取当前用户的收货地址列表（下单页选择地址用）
     */
    @GetMapping("/my")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<AddressVO>> page(Authentication authentication, org.example.ordermanagement.dto.UserAddressQueryDTO dto) {
        return Result.success(addressService.pageMyAddresses(authentication.getName(), dto));
    }

    /**
     * 根据主键ID删除某条收货地址
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(Authentication authentication, @PathVariable Long id) {
        addressService.deleteAddress(authentication.getName(), id);
        return Result.successMessage("删除地址成功");
    }
}