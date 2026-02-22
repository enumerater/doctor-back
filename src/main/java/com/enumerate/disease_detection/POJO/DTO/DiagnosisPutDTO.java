package com.enumerate.disease_detection.POJO.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisPutDTO {
    private String notes;
}
