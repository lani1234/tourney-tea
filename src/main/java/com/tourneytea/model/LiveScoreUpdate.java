package com.tourneytea.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveScoreUpdate {
    private String matchUuid;

    // Server info
    private Integer server;
    private Integer serverFromTeam;
    private Integer currentServingNumber;

    // Match status
    private Integer matchStatus;
    private Integer matchCompletedType;
    private Integer winner;
    private Integer currentGame;

    // Game scores
    private Integer teamOneGameOneScore;
    private Integer teamOneGameTwoScore;
    private Integer teamOneGameThreeScore;
    private Integer teamOneGameFourScore;
    private Integer teamOneGameFiveScore;

    private Integer teamTwoGameOneScore;
    private Integer teamTwoGameTwoScore;
    private Integer teamTwoGameThreeScore;
    private Integer teamTwoGameFourScore;
    private Integer teamTwoGameFiveScore;

    // Game status
    private String gameOneStatus;
    private String gameTwoStatus;
    private String gameThreeStatus;
    private String gameFourStatus;
    private String gameFiveStatus;

    // Court info
    private String courtUuid;
    private String courtTitle;

    // Player names (might be partial updates)
    private String teamOnePlayerOneFirstName;
    private String teamOnePlayerOneLastName;
    private String teamOnePlayerTwoFirstName;
    private String teamOnePlayerTwoLastName;

    private String teamTwoPlayerOneFirstName;
    private String teamTwoPlayerOneLastName;
    private String teamTwoPlayerTwoFirstName;
    private String teamTwoPlayerTwoLastName;

    // Timing
    private String localDateMatchStart;
    private String localDateMatchCompleted;
    private String localDateMatchPlannedStart;
    private String localDateMatchAssignedToCourt;

    // Metadata
    private LocalDateTime timestamp;
    private String eventType;
    private Object rawData;
}