package com.enumerate.disease_detection.Controller;


import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.VO.DayTemperatureVO;
import com.enumerate.disease_detection.POJO.VO.DiseaseRecordVO;
import com.enumerate.disease_detection.POJO.VO.MergeTemHum;
import com.enumerate.disease_detection.Service.DataService;
import com.enumerate.disease_detection.Service.DiseaseRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("/data")
@Slf4j
public class DataController {

    @Autowired
    private DataService dataService;


    @RequestMapping("/test")
    public Result<String> test(@RequestParam("prompt") String input) throws IOException, URISyntaxException, InterruptedException {
        log.info("=== data controller ===");

        List<Map<String, String>> prompt = List.of(
                new HashMap<>() {{
                    put("content", input);
                    put("role", "user");
                }}
        );



        String res = dataService.getData(prompt);

        return Result.success(res);
    }

    @Autowired
    private DiseaseRecordService diseaseRecordService;


    @RequestMapping("/getDiseaseRecord")
    @CrossOrigin
    public Result<List<DiseaseRecordVO>> getDiseaseRecord(){
        List<DiseaseRecordVO> diseaseRecordVO = diseaseRecordService.getDiseaseRecord();

        return Result.success(diseaseRecordVO);
    }

    @RequestMapping("/getDayTemHum")
    @CrossOrigin
    public Result<MergeTemHum> getDayTemperature(@RequestParam("area") String area) throws IOException, URISyntaxException, InterruptedException {
        MergeTemHum res = diseaseRecordService.getDayTemperature(area);

        return Result.success(res);
    }
}
