package com.enumerate.disease_detection.POJO.DTO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pics")
public class PicDTO {
    private Long id;
    private String picCode;
    private String picUrl;

}
