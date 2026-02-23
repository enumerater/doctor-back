package com.enumerate.disease_detection.POJO.VO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ToolsVO {
    private List<String> tools;
    private List<String> enabledToolIds;
}
