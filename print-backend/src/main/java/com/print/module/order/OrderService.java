package com.print.module.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.print.common.util.SecurityUtil;
import com.print.module.customer.entity.Customer;
import com.print.module.customer.mapper.CustomerMapper;
import com.print.module.mold.entity.KnifeMold;
import com.print.module.mold.mapper.MoldMapper;
import com.print.module.order.entity.Order;
import com.print.module.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final MoldMapper moldMapper;

    public OrderService(OrderMapper orderMapper, CustomerMapper customerMapper, MoldMapper moldMapper) {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.moldMapper = moldMapper;
    }

    public Page<Order> list(int pageNum, int pageSize, String keyword, Long customerId,
                            Integer shipped, String startDate, String endDate) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Order::getPrintName, keyword)
                   .or().like(Order::getOrderNo, keyword)
                   .or().like(Order::getDeliveryNo, keyword);
        }
        if (customerId != null) {
            wrapper.eq(Order::getCustomerId, customerId);
        }
        if (shipped != null) {
            wrapper.eq(Order::getShipped, shipped);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Order::getOrderDate, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Order::getOrderDate, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return orderMapper.selectPage(page, wrapper);
    }

    public Order get(Long id) {
        return orderMapper.selectById(id);
    }

    public void create(Order order) {
        // Auto-create customer if needed
        if (order.getCustomerId() == null && StringUtils.hasText(order.getCustomerName())) {
            Customer existing = customerMapper.selectOne(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerName, order.getCustomerName()));
            if (existing == null) {
                Customer c = new Customer();
                c.setCustomerName(order.getCustomerName());
                c.setPhone("");
                c.setCreateBy(SecurityUtil.getCurrentUserId());
                customerMapper.insert(c);
                order.setCustomerId(c.getId());
            } else {
                order.setCustomerId(existing.getId());
            }
        }
        // Auto-create mold if needed
        if (order.getMoldId() == null && StringUtils.hasText(order.getMoldName())) {
            KnifeMold existing = moldMapper.selectOne(
                    new LambdaQueryWrapper<KnifeMold>().eq(KnifeMold::getMoldName, order.getMoldName()));
            if (existing == null) {
                KnifeMold m = new KnifeMold();
                m.setMoldName(order.getMoldName());
                m.setShapeType("CUSTOM");
                m.setAreaCode("A");
                m.setShelfNo("1");
                m.setLayerNo("1");
                m.setPositionNo("1");
                m.setCreateBy(SecurityUtil.getCurrentUserId());
                moldMapper.insert(m);
                order.setMoldId(m.getId());
            } else {
                order.setMoldId(existing.getId());
            }
        }
        order.setOrderNo(generateOrderNo());
        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setAmount(order.getQuantity() * order.getUnitPrice());
        }
        order.setCreateBy(SecurityUtil.getCurrentUserId());
        orderMapper.insert(order);
    }

    public void update(Order order) {
        if (order.getQuantity() != null && order.getUnitPrice() != null) {
            order.setAmount(order.getQuantity() * order.getUnitPrice());
        }
        order.setUpdateBy(SecurityUtil.getCurrentUserId());
        orderMapper.updateById(order);
    }

    public void delete(Long id) {
        orderMapper.deleteById(id);
    }

    private synchronized String generateOrderNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Order last = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .like(Order::getOrderNo, "ORD-" + datePart)
                        .orderByDesc(Order::getOrderNo)
                        .last("LIMIT 1"));
        int seq = 1;
        if (last != null) {
            String lastNo = last.getOrderNo();
            seq = Integer.parseInt(lastNo.substring(lastNo.length() - 4)) + 1;
        }
        return "ORD-" + datePart + "-" + String.format("%04d", seq);
    }
}
