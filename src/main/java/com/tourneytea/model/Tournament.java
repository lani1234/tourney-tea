package com.tourneytea.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tournaments")
public class Tournament {
    @Id
    private String id;

    private String title;
    private String slug;

    @Column(name = "date_from")
    private LocalDateTime dateFrom;

    @Column(name = "date_to")
    private LocalDateTime dateTo;

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

    @Column(columnDefinition = "TEXT")
    private String rawData;
}
