package com.tourneytea.dto;

import lombok.Data;

@Data
public class StreamingService {
    private Integer serviceId;
    private String serviceName;
    private String logoUrl;
    private String liveUrl;
    private String archivedUrl;
    private String highlightsUrl;
}
