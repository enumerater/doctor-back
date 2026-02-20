package com.enumerate.disease_detection.Service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.Mapper.CropsMapper;
import com.enumerate.disease_detection.Mapper.DiseasesMapper;
import com.enumerate.disease_detection.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.POJO.VO.CropListVO;
import com.enumerate.disease_detection.POJO.VO.DiseasesPageResult;
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

    public DiseasesPageResult getDiseasesByCrop(String cropName, Integer page, Integer pageSize) {
        // 1. 参数校验：处理空值和非法值，设置默认分页参数
        if (page == null || page < 1) {
            page = 1; // 页码默认从1开始
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 20; // 每页条数默认20，限制最大100条避免性能问题
        }

        // 2. 构建分页参数（修正拼写错误pageParm -> pageParam）
        Page<DiseasesPO> pageParam = new Page<>(page, pageSize);

        // 3. 构建查询条件：eq("数据库字段名", 值)，注意字段名要和DiseasesPO的映射一致
        // 假设DiseasesPO中作物名称的字段是cropName，数据库中是crop_name（MyBatis-Plus会自动下划线转驼峰）
        QueryWrapper<DiseasesPO> queryWrapper = new QueryWrapper<DiseasesPO>()
                .eq("crop_name", cropName); // 关键修正：将name改为crop_name（作物名称字段）

        // 4. 执行分页查询（变量名page1改为diseasePage，语义更清晰）
        Page<DiseasesPO> diseasePage = diseasesMapper.selectPage(pageParam, queryWrapper);



        // 5. 封装返回结果：从分页对象中获取列表和总数
        DiseasesPageResult result = new DiseasesPageResult();
        result.setList(diseasePage.getRecords()); // 获取分页后的列表数据
        result.setTotal(diseasePage.getRecords().size()); // 获取符合条件的总条数

        return result;
    }
}
