package com.enumerate.disease_detection.MVC.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enumerate.disease_detection.MVC.Mapper.PesticideRecordMapper;
import com.enumerate.disease_detection.MVC.Mapper.FieldNoteMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.PesticideRecordPO;
import com.enumerate.disease_detection.MVC.POJO.PO.FieldNotePO;
import com.enumerate.disease_detection.MVC.Service.PlotManagementService;
import com.enumerate.disease_detection.ModelInterfaces.AnnouncementGenerate;
import com.enumerate.disease_detection.ModelInterfaces.Assistant;
import com.enumerate.disease_detection.ModelInterfaces.CommonAssisant;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlotManagementServiceImpl implements PlotManagementService {

    @Autowired
    private PesticideRecordMapper pesticideRecordMapper;

    @Autowired
    private FieldNoteMapper fieldNoteMapper;

    @Resource(name = "tongYiStreamingModel")
    private StreamingChatModel tongYiStreamingModel;

    @Resource(name = "tongYiModel")
    private OpenAiChatModel tongYiModel;

    @Override
    public List<PesticideRecordPO> getPesticideRecords(Long plotId) {
        return pesticideRecordMapper.selectList(
                new QueryWrapper<PesticideRecordPO>().eq("plot_id", plotId).orderByDesc("application_date")
        );
    }

    @Override
    public void addPesticideRecord(Long plotId, PesticideRecordPO record) {
        record.setPlotId(plotId);
        pesticideRecordMapper.insert(record);
    }

    @Override
    public void updatePesticideEffect(Long recordId, Integer evaluation, String remarks) {
        PesticideRecordPO record = PesticideRecordPO.builder()
                .id(recordId)
                .effectEvaluation(evaluation)
                .effectRemarks(remarks)
                .build();
        pesticideRecordMapper.updateById(record);
    }

    @Override
    public Page<FieldNotePO> getFieldNotes(Long plotId, String month, int page, int pageSize) {
        Page<FieldNotePO> pageParam = new Page<>(page, pageSize);
        QueryWrapper<FieldNotePO> queryWrapper = new QueryWrapper<FieldNotePO>()
                .eq("plot_id", plotId);
        
        if (month != null && !month.isEmpty()) {
            queryWrapper.apply("DATE_FORMAT(date, '%Y-%m') = {0}", month);
        }
        
        queryWrapper.orderByDesc("date");
        return fieldNoteMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public void addFieldNote(Long plotId, FieldNotePO note) {
        note.setPlotId(plotId);
        fieldNoteMapper.insert(note);
    }

    @Override
    public void deleteFieldNote(Long noteId) {
        fieldNoteMapper.deleteById(noteId);
    }


    @Override
    public String generateFieldNote(Long plotId, String theme, List<String> keywords, Map<String, Object> context) {
        String keywordStr = keywords != null ? String.join("、", keywords) : "无";
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位资深的农事记录专家。请根据以下信息，生成一段优美、专业且真实的田间随笔。")
              .append("\n主题：").append(theme)
              .append("\n关键词：").append(keywordStr);
        
        if (context != null) {
            prompt.append("\n背景上下文：");
            context.forEach((k, v) -> prompt.append("\n- ").append(k).append(": ").append(v));
        }
        
        prompt.append("\n\n要求：")
              .append("\n1. 语言通顺，情感真实，体现农人的细致观察。")
              .append("\n2. 包含对作物长势、田间环境或农事活动的具体描述。")
              .append("\n3. 字数在150-300字左右。")
              .append("\n4. 直接输出内容，不要包含“好的”、“这是生成的随笔”等修饰语。");

        log.info("AI生成随笔请求: {}", prompt);

        CommonAssisant aiServices = AiServices.builder(CommonAssisant.class)
                .chatModel(tongYiModel)
                .build();

        return aiServices.chat(prompt.toString());
    }

    @Override
    public void updatePesticide(Long plotId, Long id, PesticideRecordPO record) {
        record.setId( id);
        record.setPlotId(plotId);
        pesticideRecordMapper.update(record, new QueryWrapper<PesticideRecordPO>().eq("id", id).eq("plot_id", plotId));
    }

    @Override
    public void updateFieldNote(Long plotId, Long id, FieldNotePO record) {
        record.setId( id);
        record.setPlotId(plotId);
        fieldNoteMapper.update(record, new QueryWrapper<FieldNotePO>().eq("id", id).eq("plot_id", plotId));
    }

    @Override
    public void deletePesticide(Long id) {
        pesticideRecordMapper.deleteById(id);
    }
}
