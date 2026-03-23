package com.enumerate.disease_detection.MVC.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.MVC.Mapper.*;
import com.enumerate.disease_detection.MVC.POJO.PO.*;
import com.enumerate.disease_detection.MVC.POJO.VO.CropListVO;
import com.enumerate.disease_detection.MVC.POJO.VO.DiseasesPageResult;
import com.enumerate.disease_detection.MVC.POJO.VO.KgGraphVO;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    @Resource(name = "tongYiModel")
    private OpenAiChatModel model;

    @Autowired
    private PesticideMapper pesticideMapper;

    @Autowired
    private SolarTermMapper solarTermMapper;

    public List<CropListVO> getCrops() {
        return cropsMapper.getCrops();
    }

    /**
     * 执行全量知识图谱自动化同步（按作物维度隔离）
     */
    @Async("aiAsyncExecutor")
    public void syncKnowledgeGraph() {
        log.info("开始执行按作物隔离的知识图谱自动化同步...");

        // 1. 获取所有存在的作物
        List<CropsPO> cropsList = cropsMapper.selectList(null);
        if (cropsList.isEmpty()) {
            log.warn("未找到作物数据，同步取消");
            return;
        }

        for (CropsPO crop : cropsList) {
            String cropName = crop.getName();
            log.info("正在处理作物图谱: {}", cropName);

            // 2. 获取该作物下的所有病害
            List<DiseasesPO> diseases = diseasesMapper.selectList(
                    new LambdaQueryWrapper<DiseasesPO>().eq(DiseasesPO::getCropName, cropName));

            for (DiseasesPO disease : diseases) {
                try {
                    processSingleDisease(disease, cropName);
                } catch (Exception e) {
                    log.error("作物 {} 下的病害 {} 抽取失败: {}", cropName, disease.getDiseaseName(), e.getMessage());
                }
            }
        }
        log.info("知识图谱全量分类同步完成！");
    }

    private void processSingleDisease(DiseasesPO disease, String cropName) {
        String prompt = String.format(
                "作为农业专家，请针对【%s】描述中的【%s】提取结构化信息：\n" +
                        "【发病条件】：%s\n" +
                        "【防治方法】：%s\n\n" +
                        "要求：\n" +
                        "1. 提取提到的具体[农药/化学药剂]名称 (pesticides)\n" +
                        "2. 提取提到的[节气]名称 (solar_terms)\n" +
                        "3. 提取提到的[典型症状]短语 (symptoms)\n" +
                        "4. 必须只返回 JSON 格式，格式如下：\n" +
                        "{\"pesticides\": [\"药名1\"], \"solar_terms\": [\"节气1\"], \"symptoms\": [\"症状1\"]}",
                cropName, disease.getDiseaseName(), disease.getFactor(), disease.getPrevention()
        );

        String response = model.chat(prompt);
        // 清理可能存在的 markdown 标记
        String jsonStr = response.replaceAll("```json", "").replaceAll("```", "").trim();
        JSONObject result = JSON.parseObject(jsonStr);

        // 唯一ID生成：增加作物前缀，防止不同作物的同名病害冲突
        String diseaseNodeId = cropName + "_disease_" + disease.getId();

        // 确保病害节点本身存在
        upsertNode(diseaseNodeId, disease.getDiseaseName(), "pest", 25, disease.getIntroduction(), cropName);

        // 处理农药
        if (result.containsKey("pesticides")) {
            List<String> pesticides = result.getList("pesticides", String.class);
            if (pesticides != null) {
                for (String pName : pesticides) {
                    String pId = cropName + "_pesticide_" + pName;
                    upsertNode(pId, pName, "pesticide", 20, "防治药剂", cropName);
                    createLink(diseaseNodeId, pId, "推荐用药", cropName);
                }
            }
        }

        // 处理节气
        if (result.containsKey("solar_terms")) {
            List<String> terms = result.getList("solar_terms", String.class);
            if (terms != null) {
                for (String tName : terms) {
                    String tId = cropName + "_term_" + tName;
                    upsertNode(tId, tName, "solar_term", 18, "发病周期", cropName);
                    createLink(diseaseNodeId, tId, "高发季节", cropName);
                }
            }
        }

        // 处理症状
        if (result.containsKey("symptoms")) {
            List<String> symptoms = result.getList("symptoms", String.class);
            if (symptoms != null) {
                for (String sName : symptoms) {
                    String sId = cropName + "_symptom_" + sName;
                    upsertNode(sId, sName, "symptom", 15, "病害特征", cropName);
                    createLink(diseaseNodeId, sId, "对应症状", cropName);
                }
            }
        }
    }

    private void upsertNode(String id, String name, String type, Integer value, String details, String cropName) {
        KgNodesPO node = kgNodesMapper.selectOne(new LambdaQueryWrapper<KgNodesPO>()
                .eq(KgNodesPO::getId, id)
                .eq(KgNodesPO::getCropName, cropName));
        if (node == null) {
            kgNodesMapper.insert(KgNodesPO.builder()
                    .id(id).name(name).type(type).value(value).details(details).cropName(cropName).build());
        }
    }

    private void createLink(String source, String target, String relation, String cropName) {
        Long count = kgLinksMapper.selectCount(new LambdaQueryWrapper<KgLinksPO>()
                .eq(KgLinksPO::getSourceId, source)
                .eq(KgLinksPO::getTargetId, target)
                .eq(KgLinksPO::getCropName, cropName));
        if (count == 0) {
            kgLinksMapper.insert(KgLinksPO.builder()
                    .sourceId(source).targetId(target).relation(relation).cropName(cropName).build());
        }
    }

    public KgGraphVO getKnowledgeGraph(String cropName, String keyword, List<String> categories, Integer depth) {
        log.info("Fetching isolated knowledge graph: cropName={}, keyword={}, categories={}", cropName, keyword, categories);

        if (!StringUtils.hasText(cropName)) {
            // 如果没传作物名称，尝试从已有数据中找第一个作物作为默认值
            List<CropsPO> all = cropsMapper.selectList(new QueryWrapper<CropsPO>().last("LIMIT 1"));
            if (all.isEmpty()) return new KgGraphVO(new ArrayList<>(), new ArrayList<>());
            cropName = all.get(0).getName();
        }

        List<KgNodesPO> nodes;
        List<KgLinksPO> links;

        LambdaQueryWrapper<KgNodesPO> nodeWrapper = new LambdaQueryWrapper<>();
        nodeWrapper.eq(KgNodesPO::getCropName, cropName); // 核心物理隔离条件

        if (StringUtils.hasText(keyword)) {
            nodeWrapper.like(KgNodesPO::getName, keyword);
        }
        if (categories != null && !categories.isEmpty()) {
            nodeWrapper.in(KgNodesPO::getType, categories);
        }

        nodes = kgNodesMapper.selectList(nodeWrapper.last("LIMIT 50"));

        if (nodes.isEmpty() && !StringUtils.hasText(keyword) && (categories == null || categories.isEmpty())) {
            // 兜底策略：如果图谱库没数据，按当前作物名动态生成简版图谱
            return generateGraphFromExistingData(cropName);
        }

        if (!nodes.isEmpty()) {
            links = kgLinksMapper.selectList(new LambdaQueryWrapper<KgLinksPO>()
                    .eq(KgLinksPO::getCropName, cropName).last("LIMIT 50"));
        } else {
            links = new ArrayList<>();
        }

        return convertToVO(nodes, links);
    }

    private KgGraphVO generateGraphFromExistingData(String targetCropName) {
        List<KgNodesPO> nodes = new ArrayList<>();
        List<KgLinksPO> links = new ArrayList<>();

        // 仅处理指定作物的初始图谱
        List<CropsPO> crops = cropsMapper.selectList(new LambdaQueryWrapper<CropsPO>().eq(CropsPO::getName, targetCropName));
        if (crops.isEmpty()) return convertToVO(nodes, links);
        
        CropsPO crop = crops.get(0);
        nodes.add(KgNodesPO.builder()
                .id("crop_" + crop.getId())
                .name(crop.getName())
                .type("crop")
                .value(30)
                .details("农作物: " + crop.getName())
                .cropName(targetCropName)
                .build());

        List<DiseasesPO> diseases = diseasesMapper.selectList(new LambdaQueryWrapper<DiseasesPO>().eq(DiseasesPO::getCropName, targetCropName));
        for (DiseasesPO disease : diseases) {
            String diseaseNodeId = targetCropName + "_disease_" + disease.getId();
            nodes.add(KgNodesPO.builder()
                    .id(diseaseNodeId)
                    .name(disease.getDiseaseName())
                    .type("pest")
                    .value(20)
                    .details(disease.getIntroduction())
                    .cropName(targetCropName)
                    .build());

            links.add(KgLinksPO.builder()
                    .sourceId("crop_" + crop.getId())
                    .targetId(diseaseNodeId)
                    .relation("易发病害")
                    .cropName(targetCropName)
                    .build());
        }

        return convertToVO(nodes, links);
    }

    private KgGraphVO convertToVO(List<KgNodesPO> nodes, List<KgLinksPO> links) {
        List<KgGraphVO.LinkVO> linkVOs = links.stream()
                .map(l -> new KgGraphVO.LinkVO(l.getSourceId(), l.getTargetId(), l.getRelation()))
                .collect(Collectors.toList());
        return new KgGraphVO(nodes, linkVOs);
    }

    public List<String> getSuggestNodes(String q, String cropName) {
        if (!StringUtils.hasText(q)) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<KgNodesPO> wrapper = new LambdaQueryWrapper<KgNodesPO>()
                .like(KgNodesPO::getName, q);
        if (StringUtils.hasText(cropName)) {
            wrapper.eq(KgNodesPO::getCropName, cropName); // 建议也按作物隔离
        }

        return kgNodesMapper.selectList(wrapper.last("LIMIT 10"))
                .stream()
                .map(KgNodesPO::getName)
                .distinct()
                .collect(Collectors.toList());
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
