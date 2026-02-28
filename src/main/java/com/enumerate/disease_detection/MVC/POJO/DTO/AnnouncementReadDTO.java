package com.enumerate.disease_detection.MVC.POJO.DTO;

import com.enumerate.disease_detection.Tools.IdsDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 需要引入lombok依赖，也可以手动写getter/setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementReadDTO {
    // 绑定自定义反序列化器，确保数组能被正确解析
    @JsonDeserialize(using = IdsDeserializer.class)
    private String[] ids;
}