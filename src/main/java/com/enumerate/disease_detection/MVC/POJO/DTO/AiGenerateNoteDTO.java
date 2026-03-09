package com.enumerate.disease_detection.MVC.POJO.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiGenerateNoteDTO {
    private Long plotId;
    private String theme;
    private List<String> keywords;
    private Map<String, Object> context;
}
