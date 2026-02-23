package com.enumerate.disease_detection.POJO.DTO;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendCodeDTO {
    private String email;
    private String type;
}
