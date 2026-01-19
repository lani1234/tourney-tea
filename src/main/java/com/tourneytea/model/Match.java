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
@Table(name = "matches")
public class Match {
    @Id
    @Column(name = "match_uuid")
    private String matchUuid;

    @Column(name = "tournament_id")
    private String tournamentId;

    @Column(name = "event_uuid")
    private String eventUuid;

    @Column(name = "event_title")
    private String eventTitle;

    @Column(name = "round_text")
    private String roundText;

    @Column(name = "round_number")
    private String roundNumber;

    @Column(name = "court_title")
    private String courtTitle;

    // Team 1 Players
    @Column(name = "team_one_player_one_uuid")
    private String teamOnePlayerOneUuid;

    @Column(name = "team_one_player_one_name")
    private String teamOnePlayerOneName;

    @Column(name = "team_one_player_two_uuid")
    private String teamOnePlayerTwoUuid;

    @Column(name = "team_one_player_two_name")
    private String teamOnePlayerTwoName;

    // Team 2 Players
    @Column(name = "team_two_player_one_uuid")
    private String teamTwoPlayerOneUuid;

    @Column(name = "team_two_player_one_name")
    private String teamTwoPlayerOneName;

    @Column(name = "team_two_player_two_uuid")
    private String teamTwoPlayerTwoUuid;

    @Column(name = "team_two_player_two_name")
    private String teamTwoPlayerTwoName;

    // Game Scores
    @Column(name = "team_one_game_one_score")
    private Integer teamOneGameOneScore;

    @Column(name = "team_two_game_one_score")
    private Integer teamTwoGameOneScore;

    @Column(name = "team_one_game_two_score")
    private Integer teamOneGameTwoScore;

    @Column(name = "team_two_game_two_score")
    private Integer teamTwoGameTwoScore;

    @Column(name = "team_one_game_three_score")
    private Integer teamOneGameThreeScore;

    @Column(name = "team_two_game_three_score")
    private Integer teamTwoGameThreeScore;

    @Column(name = "team_one_game_four_score")
    private Integer teamOneGameFourScore;

    @Column(name = "team_two_game_four_score")
    private Integer teamTwoGameFourScore;

    @Column(name = "team_one_game_five_score")
    private Integer teamOneGameFiveScore;

    @Column(name = "team_two_game_five_score")
    private Integer teamTwoGameFiveScore;

    // Match Status
    @Column(name = "match_status")
    private Integer matchStatus; // 2 = Live, based on data

    @Column(name = "match_completed_type")
    private Integer matchCompletedType;

    private Integer winner;

    @Column(name = "team_one_winning_percentage")
    private Double teamOneWinningPercentage;

    // Game Status
    @Column(name = "game_one_status")
    private String gameOneStatus;

    @Column(name = "game_two_status")
    private String gameTwoStatus;

    @Column(name = "game_three_status")
    private String gameThreeStatus;

    // Timing
    @Column(name = "local_date_match_start")
    private LocalDateTime localDateMatchStart;

    @Column(name = "local_date_match_planned_start")
    private LocalDateTime localDateMatchPlannedStart;

    @Column(name = "local_date_match_completed")
    private LocalDateTime localDateMatchCompleted;

    @Column(name = "local_date_match_assigned_to_court")
    private LocalDateTime localDateMatchAssignedToCourt;

    // Server Info
    private Integer server;

    @Column(name = "server_from_team")
    private Integer serverFromTeam;

    @Column(name = "current_serving_number")
    private Integer currentServingNumber;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(columnDefinition = "TEXT")
    private String rawData;

    // Computed field for easier querying
    public boolean isLive() {
        return matchStatus != null && matchStatus == 2;
    }

    public boolean isCompleted() {
        return winner != null && winner > 0;
    }
}
