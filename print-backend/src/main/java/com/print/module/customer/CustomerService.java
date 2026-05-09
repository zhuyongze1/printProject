package com.print.module.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.print.common.util.SecurityUtil;
import com.print.module.customer.entity.Customer;
import com.print.module.customer.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerMapper customerMapper;

    public CustomerService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public Page<Customer> list(int pageNum, int pageSize, String keyword) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Customer::getCustomerName, keyword)
                   .or().like(Customer::getPhone, keyword);
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        return customerMapper.selectPage(page, wrapper);
    }

    public List<Customer> all() {
        return customerMapper.selectList(new LambdaQueryWrapper<Customer>()
                .orderByAsc(Customer::getCustomerName));
    }

    public Customer get(Long id) {
        return customerMapper.selectById(id);
    }

    public void create(Customer customer) {
        customer.setCustomerNo(generateCustomerNo());
        customer.setCreateBy(SecurityUtil.getCurrentUserId());
        customerMapper.insert(customer);
    }

    public void update(Customer customer) {
        customer.setUpdateBy(SecurityUtil.getCurrentUserId());
        customerMapper.updateById(customer);
    }

    public void delete(Long id) {
        customerMapper.deleteById(id);
    }

    private synchronized String generateCustomerNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Customer last = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .like(Customer::getCustomerNo, "CUS-" + datePart)
                        .orderByDesc(Customer::getCustomerNo)
                        .last("LIMIT 1"));
        int seq = 1;
        if (last != null) {
            String lastNo = last.getCustomerNo();
            seq = Integer.parseInt(lastNo.substring(lastNo.length() - 4)) + 1;
        }
        return "CUS-" + datePart + "-" + String.format("%04d", seq);
    }
}
