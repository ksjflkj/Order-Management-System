package org.example.ordermanagement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.example.ordermanagement.common.exception.BusinessException;
import org.example.ordermanagement.common.helper.MerchantHelper;
import org.example.ordermanagement.common.helper.UserHelper;
import org.example.ordermanagement.dto.AdminOrderQueryDTO;
import org.example.ordermanagement.dto.OrderCreateDTO;
import org.example.ordermanagement.entity.*;
import org.example.ordermanagement.mapper.*;
import org.example.ordermanagement.service.ActivityService;
import org.example.ordermanagement.service.CouponService;
import org.example.ordermanagement.service.OrderService;
import org.example.ordermanagement.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserHelper userHelper;
    private final MerchantHelper merchantHelper;
    private final UserMapper userMapper;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;
    private final MerchantMapper merchantMapper;
    private final OrdersMapper ordersMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final CouponService couponService;
    private final ActivityService activityService;
    private final CouponUserMapper couponUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(String username, OrderCreateDTO dto) {
        User user = userHelper.getByUsername(username);

        Long merchantId = dto.getMerchantId();

        // 只取指定商家的购物车记录
        List<Cart> cartList = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, user.getId())
                        .eq(Cart::getMerchantId, merchantId)
                        .orderByDesc(Cart::getCreateTime)
        );

        if (cartList == null || cartList.isEmpty()) {
            throw new BusinessException("该商家的购物车为空，无法创建订单");
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException("商家不存在或未通过审核");
        }
        // 校验营业状态：isOpen=0 表示打烊，后端必须拦截，不能仅依赖前端限制
        if (merchant.getIsOpen() == null || merchant.getIsOpen() != 1) {
            throw new BusinessException("该商家当前已打烊，暂不接受下单");
        }

        // 新增：校验地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(user.getId())) {
            throw new BusinessException("无权使用该地址");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartList) {
            Dish dish = dishMapper.selectById(cart.getDishId());
            if (dish == null) {
                throw new BusinessException("菜品不存在");
            }
            if (dish.getStatus() == null || dish.getStatus() != 1) {
                throw new BusinessException("菜品已下架：" + dish.getName());
            }
            if (dish.getStock() == null || dish.getStock() < cart.getQuantity()) {
                throw new BusinessException("菜品库存不足：" + dish.getName());
            }

            BigDecimal finalPrice = org.example.ordermanagement.common.utils.PriceUtil.calculateFinalPrice(dish.getPrice(), cart.getDishTags());
            BigDecimal itemAmount = finalPrice.multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
        }

        // 计算优惠
        BigDecimal discount = BigDecimal.ZERO;
        Long finalCouponId = null;

        // 1. 计算满减活动优惠
        BigDecimal activityDiscount = activityService.calculateDiscount(totalAmount, merchantId);

        // 2. 计算优惠券优惠
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (dto.getCouponId() != null) {
            CouponUser couponUser = couponUserMapper.selectOne(
                new LambdaQueryWrapper<CouponUser>()
                    .eq(CouponUser::getUserId, user.getId())
                    .eq(CouponUser::getCouponId, dto.getCouponId())
                    .eq(CouponUser::getStatus, "UNUSED")
            );
            if (couponUser != null) {
                Coupon coupon = couponService.getById(dto.getCouponId());
                if (coupon != null && totalAmount.compareTo(coupon.getMinAmount()) >= 0) {
                    if ("CASH".equals(coupon.getType()) || "REDUCE".equals(coupon.getType())) {
                        couponDiscount = coupon.getValue();
                    } else if ("DISCOUNT".equals(coupon.getType())) {
                        // 折扣券：原价 * (1 - 折扣值/10)
                        couponDiscount = totalAmount.multiply(
                            BigDecimal.ONE.subtract(coupon.getValue().divide(BigDecimal.TEN))
                        );
                    }
                }
            }
        }

        if (activityDiscount.compareTo(couponDiscount) >= 0) {
            discount = activityDiscount;
        } else {
            discount = couponDiscount;
            finalCouponId = dto.getCouponId();
        }

        BigDecimal finalAmount = totalAmount.subtract(discount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setUserId(user.getId());
        orders.setMerchantId(merchantId);
        orders.setTotalAmount(finalAmount);
        orders.setOriginalAmount(totalAmount); // 记录原价
        orders.setDiscountAmount(discount); // 记录优惠金额
        orders.setStatus("PENDING_PAYMENT");
        orders.setRemark(dto.getRemark());

        // 新增：保存地址快照
        orders.setAddressId(address.getId());
        orders.setContactName(address.getContactName());
        orders.setContactPhone(address.getContactPhone());
        orders.setDeliveryAddress(
                (address.getProvince() == null ? "" : address.getProvince()) +
                        (address.getCity() == null ? "" : address.getCity()) +
                        (address.getDistrict() == null ? "" : address.getDistrict()) +
                        address.getDetailAddress()
        );

        ordersMapper.insert(orders);

        for (Cart cart : cartList) {
            Dish dish = dishMapper.selectById(cart.getDishId());

            OrderItem item = new OrderItem();
            item.setOrderId(orders.getId());
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImage());
            BigDecimal finalPrice = org.example.ordermanagement.common.utils.PriceUtil.calculateFinalPrice(dish.getPrice(), cart.getDishTags());
            item.setPrice(finalPrice);
            item.setQuantity(cart.getQuantity());
            item.setAmount(finalPrice.multiply(BigDecimal.valueOf(cart.getQuantity())));
            item.setDishTags(cart.getDishTags());
            orderItemMapper.insert(item);

            dish.setStock(dish.getStock() - cart.getQuantity());
            dish.setSales((dish.getSales() == null ? 0 : dish.getSales()) + cart.getQuantity());
            dishMapper.updateById(dish);
        }

        // 只清空已结算商家的购物车记录，保留其他商家的菜品
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, user.getId())
                        .eq(Cart::getMerchantId, merchantId)
        );

        // 标记优惠券为已使用
        if (finalCouponId != null) {
            CouponUser couponUser = couponUserMapper.selectOne(
                new LambdaQueryWrapper<CouponUser>()
                    .eq(CouponUser::getUserId, user.getId())
                    .eq(CouponUser::getCouponId, finalCouponId)
                    .eq(CouponUser::getStatus, "UNUSED")
            );
            if (couponUser != null) {
                couponUser.setStatus("USED");
                couponUser.setUseTime(LocalDateTime.now());
                couponUser.setOrderId(orders.getId());
                couponUserMapper.updateById(couponUser);
            }
        }
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<OrderVO> pageMyOrders(String username, org.example.ordermanagement.dto.UserOrderQueryDTO dto) {
        User user = userHelper.getByUsername(username);

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, user.getId())
                .orderByDesc(Orders::getCreateTime);

        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Orders::getStatus, dto.getStatus());
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Orders> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        ordersMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<OrderVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<Orders> records = page.getRecords();

        // 批量查商家，消除 N+1（原：每条订单单独 selectById）
        Set<Long> merchantIdSet = records.stream()
                .map(Orders::getMerchantId)
                .collect(Collectors.toSet());
        Map<Long, Merchant> merchantMap = merchantIdSet.isEmpty()
                ? Collections.emptyMap()
                : merchantMapper.selectBatchIds(merchantIdSet).stream()
                        .collect(Collectors.toMap(Merchant::getId, Function.identity()));

        List<OrderVO> voList = records.stream().map(order -> {
            Merchant merchant = merchantMap.get(order.getMerchantId());
            String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";

            return new OrderVO(
                    order.getId(),
                    order.getOrderNo(),
                    order.getMerchantId(),
                    shopName,
                    order.getTotalAmount(),
                    order.getOriginalAmount(),
                    order.getDiscountAmount(),
                    order.getStatus(),
                    order.getRemark(),
                    order.getContactName(),
                    order.getContactPhone(),
                    order.getDeliveryAddress(),
                    order.getCreateTime()
            );
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public OrderDetailVO getOrderDetail(String username, Long orderId) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权查看该订单");
        }

        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";

        List<OrderItem> itemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByDesc(OrderItem::getCreateTime)
        );

        List<OrderItemVO> itemVOList = itemList.stream().map(item ->
                new OrderItemVO(
                        item.getDishId(),
                        item.getDishName(),
                        item.getDishImage(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getAmount(),
                        item.getDishTags()
                )
        ).collect(Collectors.toList());

        return new OrderDetailVO(
                order.getId(),
                order.getOrderNo(),
                order.getMerchantId(),
                shopName,
                order.getTotalAmount(),
                order.getStatus(),
                order.getRemark(),
                order.getContactName(),
                order.getContactPhone(),
                order.getDeliveryAddress(),
                order.getCreateTime(),
                order.getRefundReason(),
                order.getRefundTime(),
                order.getReceivedTime(),
                order.getRefundRejectReason(),
                itemVOList
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String username, Long orderId) {
        User user = userHelper.getByUsername(username);


        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权取消该订单");
        }

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }

        order.setStatus("CANCELLED");
        ordersMapper.updateById(order);

        // 退还优惠券
        CouponUser couponUser = couponUserMapper.selectOne(
                new LambdaQueryWrapper<CouponUser>()
                        .eq(CouponUser::getOrderId, orderId)
                        .eq(CouponUser::getUserId, user.getId())
        );
        if (couponUser != null) {
            LambdaUpdateWrapper<CouponUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CouponUser::getId, couponUser.getId())
                         .set(CouponUser::getStatus, "UNUSED")
                         .set(CouponUser::getUseTime, null)
                         .set(CouponUser::getOrderId, null);
            couponUserMapper.update(null, updateWrapper);
        }

        List<OrderItem> itemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );

        for (OrderItem item : itemList) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish != null) {
                dish.setStock((dish.getStock() == null ? 0 : dish.getStock()) + item.getQuantity());
                dish.setSales(Math.max((dish.getSales() == null ? 0 : dish.getSales()) - item.getQuantity(), 0));
                dishMapper.updateById(dish);
            }
        }
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String username, Long orderId) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权支付该订单");
        }

        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可支付");
        }

        order.setStatus("PAID");
        ordersMapper.updateById(order);
    }

    @Override
    public List<MerchantOrderVO> listMerchantOrders(String username) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        
        // 获取所有商家的ID
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());
        
        if (merchantIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Orders> orderList = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>()
                        .in(Orders::getMerchantId, merchantIds)
                        .orderByDesc(Orders::getCreateTime)
        );

        // 批量查用户，消除 N+1（原：每条订单单独 selectById）
        Set<Long> userIdSet = orderList.stream()
                .map(Orders::getUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userIdSet.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIdSet).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        return orderList.stream().map(order -> {
            User user = userMap.get(order.getUserId());
            String usernameValue = user != null ? user.getUsername() : "用户已不存在";

            return new MerchantOrderVO(
                    order.getId(),
                    order.getOrderNo(),
                    order.getUserId(),
                    usernameValue,
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getRemark(),
                    order.getCreateTime()
            );
        }).collect(Collectors.toList());
    }

    @Override
    public MerchantOrderDetailVO getMerchantOrderDetail(String username, Long orderId) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!merchantIds.contains(order.getMerchantId())) {
            throw new BusinessException("无权查看该订单");
        }

        User user = userMapper.selectById(order.getUserId());
        String usernameValue = user != null ? user.getUsername() : "用户已不存在";

        List<OrderItem> itemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByDesc(OrderItem::getCreateTime)
        );

        List<OrderItemVO> itemVOList = itemList.stream().map(item ->
                new OrderItemVO(
                        item.getDishId(),
                        item.getDishName(),
                        item.getDishImage(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getAmount(),
                        item.getDishTags()
                )
        ).collect(Collectors.toList());

        return new MerchantOrderDetailVO(
                order.getId(),
                order.getOrderNo(),
                order.getUserId(),
                usernameValue,
                order.getTotalAmount(),
                order.getStatus(),
                order.getRemark(),
                order.getCreateTime(),
                order.getRefundReason(),
                order.getRefundTime(),
                order.getReceivedTime(),
                order.getRefundRejectReason(),
                order.getContactName(),
                order.getContactPhone(),
                order.getDeliveryAddress(),
                itemVOList
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(String username, Long orderId) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!merchantIds.contains(order.getMerchantId())) {
            throw new BusinessException("无权操作该订单");
        }

        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可接单");
        }

        order.setStatus("ACCEPTED");
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String username, Long orderId) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        if (!merchantIds.contains(order.getMerchantId())) {
            throw new BusinessException("无权操作该订单");
        }

        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可完成");
        }

        order.setStatus("COMPLETED");
        ordersMapper.updateById(order);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<AdminOrderVO> pageAdminOrders(AdminOrderQueryDTO dto) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getOrderNo())) {
            wrapper.like(Orders::getOrderNo, dto.getOrderNo());
        }
        if (StringUtils.hasText(dto.getStatus())) {
            wrapper.eq(Orders::getStatus, dto.getStatus());
        }

        wrapper.orderByDesc(Orders::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Orders> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getCurrent(), dto.getSize());
        ordersMapper.selectPage(page, wrapper);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AdminOrderVO> voPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<Orders> adminRecords = page.getRecords();

        // 批量查用户和商家，消除双重 N+1（原：每条订单各单独 selectById）
        Set<Long> adminUserIds = adminRecords.stream()
                .map(Orders::getUserId).collect(Collectors.toSet());
        Set<Long> adminMerchantIds = adminRecords.stream()
                .map(Orders::getMerchantId).collect(Collectors.toSet());

        Map<Long, User> adminUserMap = adminUserIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(adminUserIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Merchant> adminMerchantMap = adminMerchantIds.isEmpty()
                ? Collections.emptyMap()
                : merchantMapper.selectBatchIds(adminMerchantIds).stream()
                        .collect(Collectors.toMap(Merchant::getId, Function.identity()));

        List<AdminOrderVO> voList = adminRecords.stream().map(order -> {
            User user = adminUserMap.get(order.getUserId());
            Merchant merchant = adminMerchantMap.get(order.getMerchantId());

            String username = user != null ? user.getUsername() : "用户已不存在";
            String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";

            return new AdminOrderVO(
                    order.getId(),
                    order.getOrderNo(),
                    username,
                    shopName,
                    order.getTotalAmount(),
                    order.getStatus(),
                    order.getRemark(),
                    order.getCreateTime()
            );
        }).toList();

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public AdminOrderDetailVO getAdminOrderDetail(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        User user = userMapper.selectById(order.getUserId());
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());

        String username = user != null ? user.getUsername() : "用户已不存在";
        String shopName = merchant != null ? merchant.getShopName() : "商家已不存在";

        List<OrderItem> itemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .orderByDesc(OrderItem::getCreateTime)
        );

        List<OrderItemVO> itemVOList = itemList.stream().map(item ->
                new OrderItemVO(
                        item.getDishId(),
                        item.getDishName(),
                        item.getDishImage(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getAmount(),
                        item.getDishTags()
                )
        ).toList();

        return new AdminOrderDetailVO(
                order.getId(),
                order.getOrderNo(),
                username,
                shopName,
                order.getTotalAmount(),
                order.getStatus(),
                order.getRemark(),
                order.getContactName(),
                order.getContactPhone(),
                order.getDeliveryAddress(),
                order.getRefundReason(),
                order.getRefundTime(),
                order.getRefundRejectReason(),
                order.getCreateTime(),
                itemVOList
        );
    }

    @Override
    public AdminDashboardVO getAdminDashboard() {
        long userCount = userMapper.selectCount(null);
        long merchantCount = merchantMapper.selectCount(null);
        long dishCount = dishMapper.selectCount(null);
        long orderCount = ordersMapper.selectCount(null);

        long paidOrderCount = ordersMapper.selectCount(
                new LambdaQueryWrapper<Orders>().in(Orders::getStatus, "PAID", "ACCEPTED", "COMPLETED", "RECEIVED", "REFUNDED")
        );

        List<Orders> paidOrders = ordersMapper.selectList(
                new LambdaQueryWrapper<Orders>().in(Orders::getStatus, "PAID", "ACCEPTED", "COMPLETED", "RECEIVED", "REFUNDED")
        );

        BigDecimal totalSalesAmount = paidOrders.stream()
                .map(order -> order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminDashboardVO(
                userCount,
                merchantCount,
                dishCount,
                orderCount,
                paidOrderCount,
                totalSalesAmount
        );
    }

    // ──────────── 确认收货 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceived(String username, Long orderId) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!"ACCEPTED".equals(order.getStatus())) {
            throw new BusinessException("仅商家接单后方可确认收货");
        }

        order.setStatus("RECEIVED");
        order.setReceivedTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    // ──────────── 申请退款 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(String username, Long orderId, String reason) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(user.getId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!"ACCEPTED".equals(order.getStatus()) && !"RECEIVED".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不支持退款申请");
        }

        order.setStatus("REFUND_PENDING");
        order.setRefundReason(reason);
        order.setRefundTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    // ──────────── 商家同意退款 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void agreeRefund(String username, Long orderId) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!merchantIds.contains(order.getMerchantId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!"REFUND_PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可同意退款");
        }

        // 归还菜品库存
        List<OrderItem> itemList = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
        );
        for (OrderItem item : itemList) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish != null) {
                dish.setStock((dish.getStock() == null ? 0 : dish.getStock()) + item.getQuantity());
                dish.setSales(Math.max((dish.getSales() == null ? 0 : dish.getSales()) - item.getQuantity(), 0));
                dishMapper.updateById(dish);
            }
        }

        // 归还优惠券（若有）
        CouponUser couponUser = couponUserMapper.selectOne(
                new LambdaQueryWrapper<CouponUser>()
                        .eq(CouponUser::getOrderId, orderId)
                        .eq(CouponUser::getUserId, order.getUserId())
        );
        if (couponUser != null) {
            LambdaUpdateWrapper<CouponUser> uw = new LambdaUpdateWrapper<>();
            uw.eq(CouponUser::getId, couponUser.getId())
              .set(CouponUser::getStatus, "UNUSED")
              .set(CouponUser::getUseTime, null)
              .set(CouponUser::getOrderId, null);
            couponUserMapper.update(null, uw);
        }

        order.setStatus("REFUNDED");
        ordersMapper.updateById(order);
    }

    // ──────────── 商家拒绝退款 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRefund(String username, Long orderId, String rejectReason) {
        List<Merchant> merchants = merchantHelper.getActiveMerchantsByUsername(username);
        List<Long> merchantIds = merchants.stream().map(Merchant::getId).collect(Collectors.toList());

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!merchantIds.contains(order.getMerchantId())) {
            throw new BusinessException("无权操作该订单");
        }
        if (!"REFUND_PENDING".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不可拒绝退款");
        }

        // 商家拒绝后置为 REFUND_REJECTED，用户可进一步申请平台介入
        order.setStatus("REFUND_REJECTED");
        order.setRefundRejectReason(rejectReason);
        ordersMapper.updateById(order);
    }

    // ──────────── 申请平台介入 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void escalateToArbitration(String username, Long orderId) {
        User user = userHelper.getByUsername(username);

        Orders order = ordersMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getUserId().equals(user.getId())) throw new BusinessException("无权操作该订单");
        if (!"REFUND_REJECTED".equals(order.getStatus())) {
            throw new BusinessException("仅商家拒绝退款后方可申请平台介入");
        }

        order.setStatus("REFUND_ARBITRATING");
        ordersMapper.updateById(order);
    }

    // ──────────── 平台仲裁处理 ────────────

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveArbitration(Long orderId, boolean approve, String note) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!"REFUND_ARBITRATING".equals(order.getStatus())) {
            throw new BusinessException("该订单当前不在仲裁状态");
        }

        if (approve) {
            // 平台支持退款：归还库存
            List<OrderItem> itemList = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId)
            );
            for (OrderItem item : itemList) {
                Dish dish = dishMapper.selectById(item.getDishId());
                if (dish != null) {
                    dish.setStock((dish.getStock() == null ? 0 : dish.getStock()) + item.getQuantity());
                    dish.setSales(Math.max((dish.getSales() == null ? 0 : dish.getSales()) - item.getQuantity(), 0));
                    dishMapper.updateById(dish);
                }
            }
            // 归还优惠券（若有）
            CouponUser couponUser = couponUserMapper.selectOne(
                    new LambdaQueryWrapper<CouponUser>()
                            .eq(CouponUser::getOrderId, orderId)
                            .eq(CouponUser::getUserId, order.getUserId())
            );
            if (couponUser != null) {
                LambdaUpdateWrapper<CouponUser> uw = new LambdaUpdateWrapper<>();
                uw.eq(CouponUser::getId, couponUser.getId())
                  .set(CouponUser::getStatus, "UNUSED")
                  .set(CouponUser::getUseTime, null)
                  .set(CouponUser::getOrderId, null);
                couponUserMapper.update(null, uw);
            }
            order.setStatus("REFUNDED");
        } else {
            // 平台驳回：订单恢复 ACCEPTED，商家继续履约送餐
            order.setStatus("ACCEPTED");
            order.setRefundRejectReason(note != null && !note.isBlank() ? note : "平台仲裁驳回退款申请，请等待商家送达");
        }
        ordersMapper.updateById(order);
    }
}