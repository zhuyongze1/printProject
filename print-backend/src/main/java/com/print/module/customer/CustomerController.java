package com.print.module.customer;

import com.print.common.result.PageResult;
import com.print.common.result.Result;
import com.print.module.customer.entity.Customer;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Result<PageResult<Customer>> list(@RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "20") int pageSize,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(customerService.list(pageNum, pageSize, keyword)));
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> all() {
        List<Map<String, Object>> list = customerService.all().stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "customerName", c.getCustomerName()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        return Result.success(customerService.get(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Customer customer) {
        customerService.create(customer);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        customerService.update(customer);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }
}
