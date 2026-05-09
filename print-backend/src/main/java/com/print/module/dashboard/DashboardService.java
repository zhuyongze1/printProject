package com.print.module.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.print.module.customer.mapper.CustomerMapper;
import com.print.module.mold.mapper.MoldMapper;
import com.print.module.order.entity.Order;
import com.print.module.order.mapper.OrderMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final OrderMapper orderMapper;
    private final CustomerMapper customerMapper;
    private final MoldMapper moldMapper;

    public DashboardService(OrderMapper orderMapper, CustomerMapper customerMapper, MoldMapper moldMapper) {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.moldMapper = moldMapper;
    }

    public Map<String, Object> getStatistics() {
        long totalOrders = orderMapper.selectCount(new LambdaQueryWrapper<>());
        long shippedOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getShipped, 1));
        long unshippedOrders = totalOrders - shippedOrders;
        long totalCustomers = customerMapper.selectCount(new LambdaQueryWrapper<>());
        long totalMolds = moldMapper.selectCount(new LambdaQueryWrapper<>());

        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .select(Order::getAmount));
        Double totalAmount = allOrders.stream()
                .map(Order::getAmount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", totalOrders);
        result.put("shippedOrders", shippedOrders);
        result.put("unshippedOrders", unshippedOrders);
        result.put("totalCustomers", totalCustomers);
        result.put("totalMolds", totalMolds);
        result.put("totalAmount", BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP));
        return result;
    }

    public List<Map<String, Object>> getOrderTrend() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .between(Order::getOrderDate, start.atStartOfDay(), end.plusDays(1).atStartOfDay())
                .orderByAsc(Order::getOrderDate));

        Map<String, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderDate().toString()));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String key = d.toString();
            List<Order> dayOrders = grouped.getOrDefault(key, List.of());
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", key);
            point.put("count", dayOrders.size());
            point.put("amount", dayOrders.stream().mapToDouble(o -> o.getAmount() != null ? o.getAmount() : 0).sum());
            trend.add(point);
        }
        return trend;
    }

    public List<Map<String, Object>> getCustomerRanking() {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .select(Order::getCustomerId, Order::getCustomerName, Order::getAmount));

        Map<Long, List<Order>> grouped = orders.stream()
                .filter(o -> o.getCustomerId() != null)
                .collect(Collectors.groupingBy(Order::getCustomerId));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("customerId", entry.getKey());
                    item.put("customerName", entry.getValue().get(0).getCustomerName());
                    item.put("orderCount", entry.getValue().size());
                    item.put("totalAmount", entry.getValue().stream()
                            .mapToDouble(o -> o.getAmount() != null ? o.getAmount() : 0).sum());
                    return item;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("totalAmount"), (Double) a.get("totalAmount")))
                .limit(10)
                .collect(Collectors.toList());
    }
}
