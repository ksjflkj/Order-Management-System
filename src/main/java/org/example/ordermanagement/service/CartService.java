package org.example.ordermanagement.service;

import org.example.ordermanagement.dto.CartAddDTO;
import org.example.ordermanagement.dto.CartUpdateDTO;
import org.example.ordermanagement.vo.CartVO;

import java.util.List;

public interface CartService {

    void addToCart(String username, CartAddDTO dto);

    List<CartVO> listCart(String username);

    void updateCartQuantity(String username, CartUpdateDTO dto);

    void deleteCartItem(String username, Long id);

    void clearCart(String username);
}
