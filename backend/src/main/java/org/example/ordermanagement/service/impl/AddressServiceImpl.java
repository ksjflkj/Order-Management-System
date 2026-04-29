package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.UserHelper;
import org.example.ordermanagement.dto.AddressSaveDTO;
import org.example.ordermanagement.entity.Address;
import org.example.ordermanagement.entity.User;
import org.example.ordermanagement.mapper.AddressMapper;
import org.example.ordermanagement.service.AddressService;
import org.example.ordermanagement.vo.AddressVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;
    private final UserHelper userHelper;

    @Override
    public void saveAddress(String username, AddressSaveDTO dto) {
        User user = userHelper.getByUsername(username);

        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            List<Address> oldDefaultList = addressMapper.selectList(
                    new LambdaQueryWrapper<Address>()
                            .eq(Address::getUserId, user.getId())
                            .eq(Address::getIsDefault, 1)
            );
            for (Address old : oldDefaultList) {
                old.setIsDefault(0);
                addressMapper.updateById(old);
            }
        }

        if (dto.getId() == null) {
            Address address = new Address();
            address.setUserId(user.getId());
            address.setContactName(dto.getContactName());
            address.setContactPhone(dto.getContactPhone());
            address.setProvince(dto.getProvince());
            address.setCity(dto.getCity());
            address.setDistrict(dto.getDistrict());
            address.setDetailAddress(dto.getDetailAddress());
            address.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
            addressMapper.insert(address);
            return;
        }

        Address address = addressMapper.selectById(dto.getId());
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该地址");
        }

        address.setContactName(dto.getContactName());
        address.setContactPhone(dto.getContactPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetailAddress(dto.getDetailAddress());
        address.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        addressMapper.updateById(address);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<AddressVO> pageMyAddresses(String username, org.example.ordermanagement.dto.UserAddressQueryDTO dto) {
        User user = userHelper.getByUsername(username);

        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, user.getId())
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Address> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        addressMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AddressVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<AddressVO> list = page.getRecords().stream().map(item -> new AddressVO(
                item.getId(),
                item.getContactName(),
                item.getContactPhone(),
                item.getProvince(),
                item.getCity(),
                item.getDistrict(),
                item.getDetailAddress(),
                item.getIsDefault()
        )).collect(Collectors.toList());
        
        voPage.setRecords(list);
        return voPage;
    }

    @Override
    public void deleteAddress(String username, Long id) {
        User user = userHelper.getByUsername(username);

        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(user.getId())) {
            throw new BusinessException("无权删除该地址");
        }

        addressMapper.deleteById(id);
    }
}