package com.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrdersTbl {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    /** 收货地址 JSON 快照 */
    private String addressSnapshot;

    private BigDecimal totalAmount;

    /** 0待支付 1待发货 2待收货 3已完成 4已取消 */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createdAt;

    private String orderNo;

    private String payNo;

    private LocalDateTime updatedAt;

    /**
     * 不存数据库，查询后由 Service 计算，告诉前端「这个订单现在能不能取消」。
     * 待支付：可以；已支付：只给 1 小时反悔时间。
     */
    @TableField(exist = false)
    private Boolean cancelable;

    /**
     * 不存数据库：这个订单最晚能取消到什么时候（= 支付时间 + 配置的窗口）。
     * 前端拿它算「还剩几分钟」，窗口长度只配在后端一处。
     */
    @TableField(exist = false)
    private LocalDateTime cancelDeadline;
}
