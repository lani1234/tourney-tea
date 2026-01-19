package com.tourneytea.dto;

import lombok.Data;

import java.util.List;

@Data
public class TournamentData {
    private List<TournamentItem> items;
    private int totalCount;
}
