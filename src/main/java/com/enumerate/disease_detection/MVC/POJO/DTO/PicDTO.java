package com.enumerate.disease_detection.MVC.POJO.DTO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("pics")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PicDTO {
    private Long id;
    private String picCode;
    private String picUrl;

}
