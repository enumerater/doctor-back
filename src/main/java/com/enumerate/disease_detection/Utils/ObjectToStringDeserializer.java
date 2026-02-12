package com.enumerate.disease_detection.Utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * 处理JSON对象转String的反序列化器
 */
public class ObjectToStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 如果是JSON对象，转成字符串；如果是字符串，直接返回
        if (p.getCurrentToken().isStructStart()) {
            ObjectNode node = p.readValueAsTree();
            return node.toString();
        }
        return p.getValueAsString();
    }
}