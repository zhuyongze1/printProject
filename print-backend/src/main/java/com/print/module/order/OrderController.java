package com.print.module.order;

import com.print.common.result.PageResult;
import com.print.common.result.Result;
import com.print.module.order.entity.Order;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Result<PageResult<Order>> list(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "20") int pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long customerId,
                                           @RequestParam(required = false) Integer shipped,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate) {
        return Result.success(PageResult.of(
                orderService.list(pageNum, pageSize, keyword, customerId, shipped, startDate, endDate)));
    }

    @GetMapping("/{id}")
    public Result<Order> get(@PathVariable Long id) {
        return Result.success(orderService.get(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Order order) {
        orderService.create(order);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        orderService.update(order);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return Result.success();
    }

    @PostMapping("/print")
    public Result<Void> print(@RequestBody Map<String, List<Long>> body) {
        // TODO: implement delivery note PDF generation with same-customer validation
        return Result.success();
    }
}
