package com.enumerate.disease_detection.MVC.POJO.VO;

import com.enumerate.disease_detection.MVC.POJO.PO.KgLinksPO;
import com.enumerate.disease_detection.MVC.POJO.PO.KgNodesPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KgGraphVO {
    private List<KgNodesPO> nodes;
    private List<LinkVO> links;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinkVO {
        private String source;
        private String target;
        private String relation;
    }
}
