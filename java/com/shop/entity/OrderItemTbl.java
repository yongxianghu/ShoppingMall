package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    private String image;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;
}
