package com.tourneytea.dto;

import lombok.Data;

@Data
public class TickerResponse {
    private TickerData data;
    private int statusCode;
}
