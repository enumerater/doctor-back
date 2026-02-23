package com.enumerate.disease_detection.POJO.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserNameUpdateDTO {

    private String username;
    private String avatar;
}
