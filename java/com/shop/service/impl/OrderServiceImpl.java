package com.shop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.shop.common.BusinessException;
import com.shop.entity.AddressTbl;
import com.shop.entity.CartItemTbl;
import com.shop.entity.OrderItemTbl;
import com.shop.entity.OrdersTbl;
import com.shop.entity.ProductTbl;
import com.shop.mapper.AddressMapper;
import com.shop.mapper.CartItemMapper;
import com.shop.mapper.OrderItemMapper;
import com.shop.mapper.OrdersMapper;
import com.shop.mapper.ProductMapper;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, OrdersTbl> implements OrderService {

    private static final DateTimeFormatter NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** 已支付订单还能取消多少分钟，配置在 application.properties */
    @Value("${shop.order.cancel-window-minutes:60}")
    private long cancelWindowMinutes;

    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public OrdersTbl create(Integer userId, Integer addressId) {
        AddressTbl address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException("请选择正确的收货地址");
        }

        // 只结算购物车里勾选的商品
        List<CartItemTbl> cartItems = cartItemMapper.selectList(Wrappers.<CartItemTbl>lambdaQuery()
                .eq(CartItemTbl::getUserId, userId)
                .eq(CartItemTbl::getChecked, 1));
        if (cartItems.isEmpty()) {
            throw new BusinessException("购物车里没有勾选的商品");
        }

        // 先算总价、检查库存
        BigDecimal total = BigDecimal.ZERO;
        List<ProductTbl> products = new ArrayList<>();
        for (CartItemTbl cartItem : cartItems) {
            ProductTbl product = productMapper.selectById(cartItem.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品已下架，请刷新购物车");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("《" + product.getName() + "》库存不足");
            }
            products.add(product);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        // 写订单主表
        OrdersTbl order = new OrdersTbl();
        order.setUserId(userId);
        order.setOrderNo(NO_FORMAT.format(LocalDateTime.now())
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));
        order.setTotalAmount(total);
        order.setStatus(0);
        order.setAddressSnapshot(addressJson(address));
        save(order);

        // 扣库存 + 写明细
        for (int i = 0; i < cartItems.size(); i++) {
            CartItemTbl cartItem = cartItems.get(i);
            ProductTbl product = products.get(i);

            // where stock >= ? 这一句就是防超卖的关键
            int affected = productMapper.deductStock(product.getId(), cartItem.getQuantity());
            if (affected == 0) {
                throw new BusinessException("《" + product.getName() + "》库存不足，下单失败");
            }

            OrderItemTbl item = new OrderItemTbl();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(cartItem.getQuantity());
            item.setImage(product.getMainImage());
            item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemMapper.insert(item);
        }

        // 清掉已下单的购物车行
        for (CartItemTbl cartItem : cartItems) {
            cartItemMapper.deleteById(cartItem.getId());
        }

        return order;
    }

    @Override
    public List<OrdersTbl> list(Integer userId) {
        List<OrdersTbl> orders = list(Wrappers.<OrdersTbl>lambdaQuery()
                .eq(OrdersTbl::getUserId, userId)
                .orderByDesc(OrdersTbl::getId));
        orders.forEach(this::applyCancelInfo);
        return orders;
    }

    @Override
    public Map<String, Object> detail(Integer userId, Integer id) {
        OrdersTbl order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(404, "订单不存在");
        }
        List<OrderItemTbl> items = orderItemMapper.selectList(Wrappers.<OrderItemTbl>lambdaQuery()
                .eq(OrderItemTbl::getOrderId, id));

        Map<String, Object> data = new LinkedHashMap<>();
        applyCancelInfo(order);
        data.put("order", order);
        data.put("items", items);
        return data;
    }

    @Override
    @Transactional
    public void cancel(Integer userId, Integer id) {
        OrdersTbl order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!canCancel(order)) {
            if (order.getStatus() != null && order.getStatus() == 1) {
                throw new BusinessException("已支付超过 " + windowText() + "，不能再取消了");
            }
            throw new BusinessException("当前订单状态不能取消");
        }

        // 把库存和销量还回去
        List<OrderItemTbl> items = orderItemMapper.selectList(Wrappers.<OrderItemTbl>lambdaQuery()
                .eq(OrderItemTbl::getOrderId, id));
        for (OrderItemTbl item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(4);
        updateById(order);
    }

    @Override
    public void pay(Integer userId, Integer id) {
        OrdersTbl order = getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("这个订单不能支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        order.setPayNo("PAY" + System.currentTimeMillis());
        updateById(order);
    }

    /**
     * 算出「能不能取消」和「最晚几点能取消」，一起塞给前端。
     * 待支付（0）随时可取消；已支付（1）从支付时间起算，窗口长度读配置。
     */
    private void applyCancelInfo(OrdersTbl order) {
        LocalDateTime deadline = null;
        boolean cancelable = false;

        if (order.getStatus() != null && order.getStatus() == 0) {
            cancelable = true;
        } else if (order.getStatus() != null && order.getStatus() == 1 && order.getPayTime() != null) {
            deadline = order.getPayTime().plusMinutes(cancelWindowMinutes);
            cancelable = deadline.isAfter(LocalDateTime.now());
        }

        order.setCancelable(cancelable);
        order.setCancelDeadline(deadline);
    }

    private boolean canCancel(OrdersTbl order) {
        applyCancelInfo(order);
        return Boolean.TRUE.equals(order.getCancelable());
    }

    /** 把分钟数说成人话：60 → 1 小时，30 → 30 分钟 */
    private String windowText() {
        if (cancelWindowMinutes <= 0) {
            return "0 分钟";
        }
        if (cancelWindowMinutes % 60 == 0) {
            return (cancelWindowMinutes / 60) + " 小时";
        }
        return cancelWindowMinutes + " 分钟";
    }

    /** 地址快照：下单那一刻的收货信息，手写 JSON 避免额外依赖。 */
    private String addressJson(AddressTbl address) {
        return "{\"receiver\":\"" + escape(address.getReceiver())
                + "\",\"tel\":\"" + escape(address.getTel())
                + "\",\"province\":\"" + escape(address.getProvince())
                + "\",\"city\":\"" + escape(address.getCity())
                + "\",\"district\":\"" + escape(address.getDistrict())
                + "\",\"detailAddress\":\"" + escape(address.getDetailAddress()) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
