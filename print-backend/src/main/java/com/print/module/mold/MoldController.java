package com.print.module.mold;

import com.print.common.result.PageResult;
import com.print.common.result.Result;
import com.print.module.mold.entity.KnifeMold;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/molds")
public class MoldController {

    private final MoldService moldService;

    public MoldController(MoldService moldService) {
        this.moldService = moldService;
    }

    @GetMapping
    public Result<PageResult<KnifeMold>> list(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "20") int pageSize,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String shapeType,
                                               @RequestParam(required = false) String status) {
        return Result.success(PageResult.of(moldService.list(pageNum, pageSize, keyword, shapeType, status)));
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> all() {
        List<Map<String, Object>> list = moldService.all().stream()
                .map(m -> Map.<String, Object>of("id", m.getId(), "moldName", m.getMoldName(),
                        "model", m.getModel()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<KnifeMold> get(@PathVariable Long id) {
        return Result.success(moldService.get(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody KnifeMold mold) {
        moldService.create(mold);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody KnifeMold mold) {
        mold.setId(id);
        moldService.update(mold);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        moldService.delete(id);
        return Result.success();
    }

    @PostMapping("/print")
    public Result<Void> print(@RequestBody Map<String, List<Long>> body) {
        // TODO: implement PDF label generation
        return Result.success();
    }
}
