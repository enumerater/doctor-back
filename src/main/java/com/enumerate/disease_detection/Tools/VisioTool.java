package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.ChatModel.MainModel;
import com.enumerate.disease_detection.POJO.VO.CropDiseaseAnalysisVO;
import com.enumerate.disease_detection.Utils.FastApiClientUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class VisioTool {

    @Autowired
    private MainModel mainModel;

    @Tool("视觉模型工具")
    public String visionTool(@P("imageUrl")  String imageUrl,@P("作物类型") String cropType) throws Exception {
        log.info("工具调用: 视觉模型工具，参数: imageUrl={}, cropType={}", imageUrl, cropType);


        String prompt = "理解图片，输出农业病害诊断结果，不用给如何治疗，只需要给诊断结果";

        CropDiseaseAnalysisVO cropDiseaseAnalysisVO = FastApiClientUtil.callCropDiseaseApi(imageUrl, prompt);
        String result = cropDiseaseAnalysisVO.getAnswerContent();
        String thinking = cropDiseaseAnalysisVO.getThinkingContent();
        log.info("工具结果: 视觉模型工具，思考过程: {}", thinking);
        log.info("工具结果: 视觉模型工具，结果: {}", result);

        return thinking + "\n" + result;
    }
    
    // 使用Slf4j日志记录
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VisioTool.class);
}