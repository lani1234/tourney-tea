package com.tourneytea.dto;

import lombok.Data;

@Data
public class TournamentItem {
    private String id;
    private String title;
    private String slug;
    private String dateFrom;
    private String dateTo;
    private String location;
    private String status;
    private String currency;
    private Boolean isCanceled;
    private Boolean isRegistrationClosed;
    private Boolean isTournamentCompleted;
    private Boolean isPrizeMoney;
    private Double lat;
    private Double lng;
    private String logo;
    private Double price;
    private Integer registrationCount;
}
