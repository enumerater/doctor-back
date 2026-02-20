package com.enumerate.disease_detection.POJO.DTO;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlotDTO {
    private String name;
    private String cropType;
    private String area;
    private String sowingDate;
    private String soilType;
}
