package com.print.module.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.print.module.customer.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
