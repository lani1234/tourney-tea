package com.tourneytea.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchData {
    private String matchUuid;

    // Team 1
    private String teamOnePlayerOneUuid;
    private String teamOnePlayerOneFirstName;
    private String teamOnePlayerOneLastName;
    private String teamOnePlayerOnePicture;
    private String teamOnePlayerTwoUuid;
    private String teamOnePlayerTwoFirstName;
    private String teamOnePlayerTwoLastName;

    // Team 2
    private String teamTwoPlayerOneUuid;
    private String teamTwoPlayerOneFirstName;
    private String teamTwoPlayerOneLastName;
    private String teamTwoPlayerOnePicture;
    private String teamTwoPlayerTwoUuid;
    private String teamTwoPlayerTwoFirstName;
    private String teamTwoPlayerTwoLastName;

    // Scores
    private Integer teamOneGameOneScore;
    private Integer teamTwoGameOneScore;
    private Integer teamOneGameTwoScore;
    private Integer teamTwoGameTwoScore;
    private Integer teamOneGameThreeScore;
    private Integer teamTwoGameThreeScore;
    private Integer teamOneGameFourScore;
    private Integer teamTwoGameFourScore;
    private Integer teamOneGameFiveScore;
    private Integer teamTwoGameFiveScore;

    // Match Info
    private Integer matchStatus;
    private Integer matchCompletedType;
    private Integer winner;
    private Double teamOneWinningPercentage;
    private String roundText;
    private String roundNumber;
    private String courtTitle;

    // Event Info
    private String eventUuid;
    private String eventTitle;
    private String tournamentTitle;

    // Status
    private String gameOneStatus;
    private String gameTwoStatus;
    private String gameThreeStatus;

    // Server Info
    private Integer server;
    private Integer serverFromTeam;
    private Integer currentServingNumber;

    // Timing
    private String localDateMatchStart;
    private String localDateMatchPlannedStart;
    private String localDateMatchCompleted;
    private String localDateMatchAssignedToCourt;

    // Streaming
    private List<StreamingService> streamingServices;
}
