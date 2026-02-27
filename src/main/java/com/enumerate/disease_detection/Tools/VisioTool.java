package com.enumerate.disease_detection.Tools;

import com.enumerate.disease_detection.Annotations.ToolName;
import com.enumerate.disease_detection.POJO.VO.CropDiseaseAnalysisVO;
import com.enumerate.disease_detection.Utils.FastApiClientUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VisioTool {

    @Tool("农作物病害图像识别工具，通过分析作物图片来诊断可能的病害。当用户提供了作物图片URL并希望识别病害时应调用此工具。输入图片URL和作物类型，返回病害诊断结果。")
    @ToolName("视觉模型工具")
    public String visionTool(
            @P("作物图片的URL地址") String imageUrl,
            @P("作物类型，如水稻、小麦、玉米等") String cropType) throws Exception {
        log.info("工具调用: 视觉模型工具，参数: imageUrl={}, cropType={}", imageUrl, cropType);

        String prompt = "理解图片，输出农业病害诊断结果，不用给如何治疗，只需要给诊断结果";

        CropDiseaseAnalysisVO cropDiseaseAnalysisVO = FastApiClientUtil.callCropDiseaseApi(imageUrl, prompt);
        String result = cropDiseaseAnalysisVO.getAnswerContent();
        String thinking = cropDiseaseAnalysisVO.getThinkingContent();
        log.info("工具结果: 视觉模型工具，思考过程: {}", thinking);
        log.info("工具结果: 视觉模型工具，结果: {}", result);

        return thinking + "\n" + result;
    }
}
