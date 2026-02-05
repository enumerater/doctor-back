package com.enumerate.disease_detection.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.enumerate.disease_detection.ChatModel.MainModel;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.PicMapper;
import com.enumerate.disease_detection.POJO.DTO.PicDTO;
import com.enumerate.disease_detection.POJO.VO.CropDiseaseAnalysisVO;
import com.enumerate.disease_detection.POJO.VO.PicVO;
import com.enumerate.disease_detection.POJO.VO.VisionVO;
import com.enumerate.disease_detection.Properties.AiModelProperties;
import com.enumerate.disease_detection.ModelInterfaces.VisionAssisant;
import com.enumerate.disease_detection.Utils.FastApiClientUtil;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/vision")
@Slf4j
public class VisionController {

    @Autowired
    private MainModel mainModel;


    @Autowired
    private AiModelProperties aiModelProperties;

//    @GetMapping
//    @CrossOrigin
//    public com.enumerate.disease_detection.Common.Result<String> vision(@RequestParam(value = "url", required = false) String url,
//                                                                @RequestParam(value = "cropType", required = false) String cropType) throws NoApiKeyException, UploadFileException {
//
//        String text = "请基于图片内容，给出" + cropType + "的识别结果，并给出识别结果的解释。简要回复即可";
//
//        MultiModalConversation conv = new MultiModalConversation();
//        MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
//                .content(Arrays.asList(
//                        Collections.singletonMap("image", url),
//                        Collections.singletonMap("text", text))).build();
//        MultiModalConversationParam param = MultiModalConversationParam.builder()
//                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
//                // 各地域的API Key不同。获取API Key：https://help.aliyun.com/zh/model-studio/get-api-key
//                .apiKey(aiModelProperties.getTong().getApiKey())
//                .model("qwen3-vl-plus")  // 此处以qwen3-vl-plus为例，可按需更换模型名称。模型列表：https://help.aliyun.com/zh/model-studio/models
//                .messages(Collections.singletonList(userMessage))
//                .build();
//        MultiModalConversationResult result = conv.call(param);
//
//        log.info("dashScope result: {}", result);
//        log.info("dashScope result: {}", result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text"));
//
//
//        return Result.success(result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString());
//    };

    @GetMapping
    @CrossOrigin
    public Result<VisionVO> vision(@RequestParam(value = "url", required = false) String url,
                                   @RequestParam(value = "cropType", required = false) String cropType) throws Exception {

        String prompt = "理解图片，输出农业病害诊断结果";

        CropDiseaseAnalysisVO cropDiseaseAnalysisVO = FastApiClientUtil.callCropDiseaseApi(url, prompt);
        String result = cropDiseaseAnalysisVO.getAnswerContent();
        String thinking = cropDiseaseAnalysisVO.getThinkingContent();
        log.info("python思考结果: 视觉模型工具，思考过程: {}", thinking);
        log.info("python思考结果: 视觉模型工具，结果: {}", result);

        // 4. 创建AI服务并调用（与纯文本调用一致，框架自动处理多模态）
        VisionAssisant visionAssisant = AiServices.create(VisionAssisant.class, mainModel.visionModel());
        VisionVO res = visionAssisant.visionChat("think " + thinking + "\n" + "result " + result);

        log.info("视觉模型返回结果：{}", res);

        return Result.success(res);
    }


    @Autowired
    private PicMapper picMapper;

    @GetMapping("/getPic")
    @CrossOrigin
    public Result<PicVO> getPic(@RequestParam(value = "picCode", required = false) String urlCode) {
        PicDTO picDTO = picMapper.selectOne(new QueryWrapper<PicDTO>().eq("pic_code", urlCode));
        if (picDTO == null) {
            return Result.success(null);
        }
        PicVO picVO = new PicVO();
        picVO.setUrl(picDTO.getPicUrl());

        return Result.success(picVO);
    }

    @GetMapping("/savePic")
    @CrossOrigin
    public Result<String> setPic(@RequestParam(value = "picCode", required = false) String urlCode,
                                 @RequestParam(value = "picUrl", required = false) String picUrl) {
        PicDTO picDTO = new PicDTO();
        picDTO.setPicCode(urlCode);
        picDTO.setPicUrl(picUrl);
        picMapper.insert(picDTO);
        return Result.success(urlCode);
    }


}
