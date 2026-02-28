package com.enumerate.disease_detection.MVC.POJO.VO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnounceTypes {
    private Integer system;
    private Integer disease_alert;
    private Integer treatment_remind;
}
