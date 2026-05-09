package com.print.module.mold;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.print.common.util.SecurityUtil;
import com.print.module.mold.entity.KnifeMold;
import com.print.module.mold.mapper.MoldMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MoldService {

    private final MoldMapper moldMapper;

    public MoldService(MoldMapper moldMapper) {
        this.moldMapper = moldMapper;
    }

    public Page<KnifeMold> list(int pageNum, int pageSize, String keyword, String shapeType, String status) {
        Page<KnifeMold> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnifeMold> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(KnifeMold::getMoldName, keyword)
                   .or().like(KnifeMold::getMoldNo, keyword)
                   .or().like(KnifeMold::getModel, keyword);
        }
        if (StringUtils.hasText(shapeType)) {
            wrapper.eq(KnifeMold::getShapeType, shapeType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(KnifeMold::getStatus, status);
        }
        wrapper.orderByDesc(KnifeMold::getCreateTime);
        return moldMapper.selectPage(page, wrapper);
    }

    public List<KnifeMold> all() {
        return moldMapper.selectList(new LambdaQueryWrapper<KnifeMold>()
                .eq(KnifeMold::getStatus, "IN_STOCK")
                .orderByAsc(KnifeMold::getMoldName));
    }

    public KnifeMold get(Long id) {
        return moldMapper.selectById(id);
    }

    public void create(KnifeMold mold) {
        mold.setMoldNo(generateMoldNo());
        mold.setModel(generateModel(mold));
        mold.setLocationCode(generateLocationCode(mold));
        mold.setCreateBy(SecurityUtil.getCurrentUserId());
        moldMapper.insert(mold);
    }

    public void update(KnifeMold mold) {
        mold.setModel(generateModel(mold));
        mold.setLocationCode(generateLocationCode(mold));
        mold.setUpdateBy(SecurityUtil.getCurrentUserId());
        moldMapper.updateById(mold);
    }

    public void delete(Long id) {
        moldMapper.deleteById(id);
    }

    public String generateModel(KnifeMold mold) {
        return switch (mold.getShapeType()) {
            case "RECTANGLE", "SQUARE" ->
                String.format("%.0f×%.0f", mold.getLength(), mold.getWidth());
            case "CIRCLE" ->
                "Φ" + String.format("%.0f", mold.getDiameter());
            case "OVAL" ->
                "OV-" + String.format("%.0f", mold.getLength()) + "×" + String.format("%.0f", mold.getWidth());
            case "CUSTOM" ->
                "CUSTOM-" + mold.getMoldName();
            default -> mold.getMoldName();
        };
    }

    public String generateLocationCode(KnifeMold mold) {
        try {
            String area = mold.getAreaCode();
            String shelf = String.format("%02d", Integer.parseInt(mold.getShelfNo()));
            String layer = String.format("%02d", Integer.parseInt(mold.getLayerNo()));
            String position = String.format("%02d", Integer.parseInt(mold.getPositionNo()));
            return area + "-" + shelf + "-" + layer + "-" + position;
        } catch (NumberFormatException e) {
            return mold.getAreaCode() + "-" + mold.getShelfNo() + "-" + mold.getLayerNo() + "-" + mold.getPositionNo();
        }
    }

    private synchronized String generateMoldNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        KnifeMold last = moldMapper.selectOne(
                new LambdaQueryWrapper<KnifeMold>()
                        .like(KnifeMold::getMoldNo, "DM-" + datePart)
                        .orderByDesc(KnifeMold::getMoldNo)
                        .last("LIMIT 1"));
        int seq = 1;
        if (last != null) {
            String lastNo = last.getMoldNo();
            seq = Integer.parseInt(lastNo.substring(lastNo.length() - 4)) + 1;
        }
        return "DM-" + datePart + "-" + String.format("%04d", seq);
    }
}
