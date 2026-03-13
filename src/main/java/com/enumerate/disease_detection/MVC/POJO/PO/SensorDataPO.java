package com.enumerate.disease_detection.MVC.POJO.PO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("sensor_data")
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long plotId;
    private Float temperature;
    private Float humidity;
    private Float npkN;
    private Float npkP;
    private Float npkK;
    private Float lightIntensity;
    private Float soilMoisture;
    private Boolean isIrrigating;
    private String description;
    private LocalDateTime recordedAt;
}
