package com.enumerate.disease_detection.MVC.POJO.VO;

import com.enumerate.disease_detection.MVC.POJO.PO.AnnouncementPO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementReadVO {
    private Integer total;
    private AnnounceTypes types;
}

