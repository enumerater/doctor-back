package com.enumerate.disease_detection.MVC.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.MVC.Mapper.CropsMapper;
import com.enumerate.disease_detection.MVC.Mapper.DiseasesMapper;
import com.enumerate.disease_detection.MVC.Mapper.KgLinksMapper;
import com.enumerate.disease_detection.MVC.Mapper.KgNodesMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.CropsPO;
import com.enumerate.disease_detection.MVC.POJO.PO.DiseasesPO;
import com.enumerate.disease_detection.MVC.POJO.PO.KgLinksPO;
import com.enumerate.disease_detection.MVC.POJO.PO.KgNodesPO;
import com.enumerate.disease_detection.MVC.POJO.VO.CropListVO;
import com.enumerate.disease_detection.MVC.POJO.VO.DiseasesPageResult;
import com.enumerate.disease_detection.MVC.POJO.VO.KgGraphVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeService {

    @Autowired
    private CropsMapper cropsMapper;

    @Autowired
    private DiseasesMapper diseasesMapper;

    @Autowired
    private KgNodesMapper kgNodesMapper;

    @Autowired
    private KgLinksMapper kgLinksMapper;

    public List<CropListVO> getCrops() {
        return cropsMapper.getCrops();
    }

    public KgGraphVO getKnowledgeGraph(String keyword, List<String> categories, Integer depth) {
        log.info("Fetching knowledge graph: keyword={}, categories={}, depth={}", keyword, categories, depth);

        // Try to fetch from kg_nodes first
        List<KgNodesPO> nodes = new ArrayList<>();
        List<KgLinksPO> links = new ArrayList<>();

        LambdaQueryWrapper<KgNodesPO> nodeWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            nodeWrapper.like(KgNodesPO::getName, keyword);
        }
        if (categories != null && !categories.isEmpty()) {
            nodeWrapper.in(KgNodesPO::getType, categories);
        }

        nodes = kgNodesMapper.selectList(nodeWrapper);

        if (nodes.isEmpty() && !StringUtils.hasText(keyword) && (categories == null || categories.isEmpty())) {
            // Fallback: Generate from crops and diseases if kg_nodes is empty
            return generateGraphFromExistingData();
        }

        if (!nodes.isEmpty()) {
            List<String> nodeIds = nodes.stream().map(KgNodesPO::getId).collect(Collectors.toList());
            LambdaQueryWrapper<KgLinksPO> linkWrapper = new LambdaQueryWrapper<>();
            linkWrapper.in(KgLinksPO::getSourceId, nodeIds).or().in(KgLinksPO::getTargetId, nodeIds);
            links = kgLinksMapper.selectList(linkWrapper);
        }

        return convertToVO(nodes, links);
    }

    private KgGraphVO generateGraphFromExistingData() {
        List<KgNodesPO> nodes = new ArrayList<>();
        List<KgLinksPO> links = new ArrayList<>();

        List<CropsPO> crops = cropsMapper.selectList(null);
        for (CropsPO crop : crops) {
            nodes.add(KgNodesPO.builder()
                    .id("crop_" + crop.getId())
                    .name(crop.getName())
                    .type("crop")
                    .value(30)
                    .details("农作物: " + crop.getName())
                    .build());
        }

        List<DiseasesPO> diseases = diseasesMapper.selectList(null);
        for (DiseasesPO disease : diseases) {
            String diseaseNodeId = "disease_" + disease.getId();
            nodes.add(KgNodesPO.builder()
                    .id(diseaseNodeId)
                    .name(disease.getDiseaseName())
                    .type("pest")
                    .value(20)
                    .details(disease.getIntroduction())
                    .build());

            // Link to crop
            if (StringUtils.hasText(disease.getCropName())) {
                crops.stream()
                        .filter(c -> c.getName().equals(disease.getCropName()))
                        .findFirst()
                        .ifPresent(c -> {
                            links.add(KgLinksPO.builder()
                                    .sourceId("crop_" + c.getId())
                                    .targetId(diseaseNodeId)
                                    .relation("易发病害")
                                    .build());
                        });
            }
        }

        return convertToVO(nodes, links);
    }

    private KgGraphVO convertToVO(List<KgNodesPO> nodes, List<KgLinksPO> links) {
        List<KgGraphVO.LinkVO> linkVOs = links.stream()
                .map(l -> new KgGraphVO.LinkVO(l.getSourceId(), l.getTargetId(), l.getRelation()))
                .collect(Collectors.toList());
        return new KgGraphVO(nodes, linkVOs);
    }

    public List<String> getSuggestNodes(String q) {
        if (!StringUtils.hasText(q)) {
            return new ArrayList<>();
        }

        // Try kg_nodes first
        List<String> suggestions = kgNodesMapper.selectList(new LambdaQueryWrapper<KgNodesPO>()
                .like(KgNodesPO::getName, q)
                .last("LIMIT 10"))
                .stream()
                .map(KgNodesPO::getName)
                .collect(Collectors.toList());

        if (suggestions.isEmpty()) {
            // Fallback to crops and diseases
            suggestions.addAll(cropsMapper.selectList(new LambdaQueryWrapper<CropsPO>()
                    .like(CropsPO::getName, q)
                    .last("LIMIT 5"))
                    .stream()
                    .map(CropsPO::getName)
                    .toList());

            suggestions.addAll(diseasesMapper.selectList(new QueryWrapper<DiseasesPO>()
                    .like("disease_name", q)
                    .last("LIMIT 5"))
                    .stream()
                    .map(DiseasesPO::getDiseaseName)
                    .toList());
        }

        return suggestions.stream().distinct().collect(Collectors.toList());
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
