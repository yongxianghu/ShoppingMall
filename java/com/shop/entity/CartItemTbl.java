package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Long productId;

    private Integer quantity;

    /** 0 未勾选 1 勾选 */
    private Integer checked;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
