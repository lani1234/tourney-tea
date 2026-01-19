package com.tourneytea.dto;

import lombok.Data;

@Data
public class TournamentResponse {
    private TournamentData data;
    private int statusCode;
}
