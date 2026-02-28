package com.enumerate.disease_detection.MVC.Controller;

import com.enumerate.disease_detection.ChatModel.MainModel;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.MVC.Mapper.PicMapper;
import com.enumerate.disease_detection.MVC.POJO.VO.CropDiseaseAnalysisVO;
import com.enumerate.disease_detection.MVC.POJO.VO.PicVO;
import com.enumerate.disease_detection.Properties.AiModelProperties;
import com.enumerate.disease_detection.Utils.FastApiClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    public Result<String> vision(@RequestParam(value = "url", required = false) String url,
                                   @RequestParam(value = "cropType", required = false) String cropType) throws Exception {

        String prompt = """
                # 任务指令
                你是一个农作物图像识别专家，需要完成以下任务：
                1. 识别上传图片中的主体内容：判断是「健康作物」「不健康作物」还是「非作物」；
                2. 若为「不健康作物」，需明确说明：病害/问题名称 + 判断依据（仅核心视觉特征，1-2句话）；
                3. 若为「非作物」，简要说明主体类型（如：石头、人物、空背景，仅1个关键词）；
                4. 所有输出必须严格遵循指定JSON格式，禁止添加任何额外解释、标点或冗余内容。
                
                # 输出格式要求（必须严格遵守）
                {
                  "type": "健康作物|不健康作物|非作物",
                  "detail": "核心描述（按以下规则填写）"
                }
                
                # detail字段填写规则
                - 健康作物：空字符串（""）；
                - 不健康作物：格式为「病害名称：判断依据」（例：霜霉病：叶片出现黄色霉层，背面有白色菌丝）；
                - 非作物：仅1个核心关键词（例：石头、人物）。
                
                # 输出示例
                示例1（健康作物）：{"type":"健康作物","detail":""}
                示例2（不健康作物）：{"type":"不健康作物","detail":"蚜虫虫害：叶片表面有大量黑色蚜虫，伴随叶片卷曲"}
                示例3（非作物）：{"type":"非作物","detail":"石头"}
                示例4（不健康作物）：{"type":"不健康作物","detail":"叶斑病：叶片出现圆形褐色病斑，边缘清晰"}
                
                # 核心要求
                - 内容极简：detail字段总长度不超过30个字；
                - 格式唯一：仅输出JSON字符串，无其他任何文字；
                - 分类准确：严格区分「健康作物」「不健康作物」「非作物」三类，不新增分类；
                - 依据具体：判断依据仅描述图片中可见的视觉特征，不做额外推测。
                """;

        CropDiseaseAnalysisVO cropDiseaseAnalysisVO = FastApiClientUtil.callCropDiseaseApi(url, prompt);
        String result = cropDiseaseAnalysisVO.getAnswerContent();
        String thinking = cropDiseaseAnalysisVO.getThinkingContent();
        log.info("python思考结果: 视觉模型工具，思考过程: {}", thinking);
        log.info("python思考结果: 视觉模型工具，结果: {}", result);

        return Result.success(result);
//
//        // 4. 创建AI服务并调用（与纯文本调用一致，框架自动处理多模态）
//        VisionAssisant visionAssisant = AiServices.create(VisionAssisant.class, mainModel.visionModel());
//        VisionVO res = visionAssisant.visionChat("think " + thinking + "\n" + "result " + result);
//
//        log.info("视觉模型返回结果：{}", res);


    }


    @Autowired
    private PicMapper picMapper;

    @GetMapping("/getPic")
    @CrossOrigin
    public Result<PicVO> getPic(@RequestParam(value = "picCode", required = false) String urlCode) {
        log.info("获取图片：{}", urlCode);

        return null ;

//        PicDTO picDTO = picMapper.selectOne(new QueryWrapper<PicDTO>().eq("pic_code", urlCode));
//        if (picDTO == null) {
//            return Result.success(null);
//        }
//        PicVO picVO = new PicVO();
//        picVO.setUrl(picDTO.getPicUrl());
//
//        return Result.success(picVO);
    }

    @GetMapping("/savePic")
    @CrossOrigin
    public Result<String> setPic(@RequestParam(value = "picCode", required = false) String urlCode,
                                 @RequestParam(value = "picUrl", required = false) String picUrl) {

        return null;
//        log.info("保存图片：{}", urlCode);
//
//        PicDTO picDTO = new PicDTO();
//        picDTO.setPicCode(urlCode);
//        picDTO.setPicUrl(picUrl);
//        picMapper.insert(picDTO);
//        return Result.success(urlCode);
    }


}
