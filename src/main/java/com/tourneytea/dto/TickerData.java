package com.tourneytea.dto;

import lombok.Data;

import java.util.List;

@Data
public class TickerData {
    private List<MatchData> matches;
    private int totalRecords;
}