package com.huatian.gmall.order.mapper;

import com.huatian.gmall.bean.OrderInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

public interface OrderInfoMapper extends Mapper<OrderInfo>{


    void delCheckedCart(@Param("delCartStr") String delCartStr);
}
