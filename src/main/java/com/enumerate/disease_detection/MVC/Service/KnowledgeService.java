package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.MVC.Mapper.CropsMapper;
import com.enumerate.disease_detection.MVC.Mapper.DiseasesMapper;

import com.enumerate.disease_detection.MVC.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.MVC.POJO.VO.CropListVO;
import com.enumerate.disease_detection.MVC.POJO.VO.DiseasesPageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class KnowledgeService {

    @Autowired
    private CropsMapper cropsMapper;

    @Autowired
    private DiseasesMapper diseasesMapper;

    public List<CropListVO> getCrops() {
        return cropsMapper.getCrops();
    }

    // 假设你的 mapper 和返回结果类已经正确引入
    public DiseasesPageResult getDiseasesByCrop(String category, String cropName, String keyword, Integer page, Integer pageSize) {
        // 1. 参数校验：处理空值和非法值，设置默认分页参数
        if (page == null || page < 1) {
            page = 1; // 页码默认从1开始
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20; // 每页条数默认20，限制最大100条避免性能问题
        }

        // 2. 构建分页参数
        Page<DiseasesPO> pageParam = new Page<>(page, pageSize);

        // 3. 构建查询条件（合并冗余逻辑）
        QueryWrapper<DiseasesPO> queryWrapper = new QueryWrapper<>();
        // 仅当 cropName 非空时才添加作物名称的查询条件
        if (StringUtils.hasText(cropName)) {
            queryWrapper.eq("crop_name", cropName);
        }
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like("disease_name", keyword);
        }
        if (StringUtils.hasText(category)) {
            queryWrapper.like("category", category);
        }

        // 4. 执行分页查询
        Page<DiseasesPO> diseasePage = diseasesMapper.selectPage(pageParam, queryWrapper);

        // 5. 封装返回结果（关键修复：总条数使用 getTotal()）
        DiseasesPageResult result = new DiseasesPageResult();
        result.setList(diseasePage.getRecords()); // 当前页的数据列表
        result.setTotal((int) diseasePage.getTotal());   // 符合条件的总记录数（核心修复点）

        return result;
    }

    public List<String> getCropName(String category) {

        return diseasesMapper.selectList(new QueryWrapper<DiseasesPO>().select("crop_name").eq("category", category)).stream().map(DiseasesPO::getCropName).distinct().toList();
    }
}
