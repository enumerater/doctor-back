package com.enumerate.disease_detection.Tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * 专门适配你的数组参数：{"ids":["ID1","ID2"]}
 * 同时兼容 "all" 字符串、单个ID字符串、逗号分隔字符串
 */
public class IdsDeserializer extends JsonDeserializer<String[]> {
    @Override
    public String[] deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        // 处理你传递的数组格式（核心适配）
        if (node.isArray()) {
            String[] ids = new String[node.size()];
            for (int i = 0; i < node.size(); i++) {
                ids[i] = node.get(i).asText(); // 逐个解析数组元素为字符串
            }
            return ids;
        }

        // 兼容其他格式（all/单个ID/逗号分隔）
        if (node.isTextual()) {
            String value = node.asText().trim();
            if ("all".equals(value)) {
                return new String[]{"all"};
            } else if (value.contains(",")) {
                return value.split(",");
            } else if (!value.isEmpty()) {
                return new String[]{value};
            }
        }

        return new String[0]; // 空值返回空数组，后续校验
    }
}