package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.AddressSaveDTO;
import org.example.ordermanagement.vo.AddressVO;

import java.util.List;

public interface AddressService {

    void saveAddress(String username, AddressSaveDTO dto);

    com.baomidou.mybatisplus.core.metadata.IPage<AddressVO> pageMyAddresses(String username, org.example.ordermanagement.dto.UserAddressQueryDTO dto);

    void deleteAddress(String username, Long id);
}