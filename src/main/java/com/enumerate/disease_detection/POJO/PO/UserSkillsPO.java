package com.enumerate.disease_detection.POJO.PO;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("user_skills")
public class UserSkillsPO {
    private Long id;
    private Long userId;
    private Long skillId;
    private Boolean enabled;

}
