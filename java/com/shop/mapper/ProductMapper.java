package com.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.entity.ProductTbl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<ProductTbl> {

    /**
     * 扣库存 + 加销量，一条 SQL 完成，靠 where 条件防超卖。
     * 返回受影响行数：0 表示库存不足或商品不存在。
     */
    @Update("update product_tbl set stock = stock - #{qty}, sales = sales + #{qty} " +
            "where id = #{id} and stock >= #{qty}")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);

    /**
     * 取消订单时回滚库存与销量。
     */
    @Update("update product_tbl set stock = stock + #{qty}, sales = sales - #{qty} " +
            "where id = #{id} and sales >= #{qty}")
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);
}
