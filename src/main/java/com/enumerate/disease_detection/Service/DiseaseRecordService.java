package com.enumerate.disease_detection.Service;


import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.Local.UserContextHolder;
import com.enumerate.disease_detection.Mapper.DiseaseRecordMapper;
import com.enumerate.disease_detection.ModelInterfaces.Assistant;
import com.enumerate.disease_detection.ModelInterfaces.SearchAssisant;
import com.enumerate.disease_detection.POJO.VO.DayTemperatureVO;
import com.enumerate.disease_detection.POJO.VO.DiseaseRecordVO;
import com.enumerate.disease_detection.POJO.VO.MergeTemHum;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DiseaseRecordService {

    @Autowired
    private DiseaseRecordMapper diseaseRecordMapper;


    public List<DiseaseRecordVO> getDiseaseRecord() {
        log.info("=== get disease record ===");
        log.info("user id: {}", UserContextHolder.getUserId());

        return diseaseRecordMapper.getDiseaseRecord(UserContextHolder.getUserId());
    }
    @Autowired
    private DataService dataService;

    @Autowired
    private MainModel mainModel;


    public MergeTemHum getDayTemperature(String area) throws IOException, URISyntaxException, InterruptedException {
        List<Map<String, String>> prompt = List.of(
                new HashMap<>() {{
                    put("content", area+ "当日每时刻温度和湿度情况0：00-23：00");
                    put("role", "user");
                }}
        );

        String response = dataService.getData(prompt);
        log.info("Search=============response: {}", response);

        SearchAssisant assistant = AiServices.builder(SearchAssisant.class)
                .chatModel(mainModel.tongYiModel())
                .build();


        return assistant.getDayTemHum(response);
    }
}
