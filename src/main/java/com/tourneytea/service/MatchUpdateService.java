package com.tourneytea.service;

import com.tourneytea.model.LiveScoreUpdate;
import com.tourneytea.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import com.tourneytea.model.Match;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchUpdateService {

    private final MatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void processUpdate(LiveScoreUpdate update) {
        log.info("Processing live update for match: {}", update.getMatchUuid());

        matchRepository.findById(update.getMatchUuid())
                .ifPresentOrElse(
                        match -> updateExistingMatch(match, update),
                        () -> log.warn("Match not found in database: {}", update.getMatchUuid())
                );
    }

    private void updateExistingMatch(Match match, LiveScoreUpdate update) {
        boolean hasChanges = false;

        // Update game scores if present
        if (update.getTeamOneGameOneScore() != null &&
                !update.getTeamOneGameOneScore().equals(match.getTeamOneGameOneScore())) {
            match.setTeamOneGameOneScore(update.getTeamOneGameOneScore());
            hasChanges = true;
        }
        if (update.getTeamTwoGameOneScore() != null &&
                !update.getTeamTwoGameOneScore().equals(match.getTeamTwoGameOneScore())) {
            match.setTeamTwoGameOneScore(update.getTeamTwoGameOneScore());
            hasChanges = true;
        }

        if (update.getTeamOneGameTwoScore() != null &&
                !update.getTeamOneGameTwoScore().equals(match.getTeamOneGameTwoScore())) {
            match.setTeamOneGameTwoScore(update.getTeamOneGameTwoScore());
            hasChanges = true;
        }
        if (update.getTeamTwoGameTwoScore() != null &&
                !update.getTeamTwoGameTwoScore().equals(match.getTeamTwoGameTwoScore())) {
            match.setTeamTwoGameTwoScore(update.getTeamTwoGameTwoScore());
            hasChanges = true;
        }

        if (update.getTeamOneGameThreeScore() != null &&
                !update.getTeamOneGameThreeScore().equals(match.getTeamOneGameThreeScore())) {
            match.setTeamOneGameThreeScore(update.getTeamOneGameThreeScore());
            hasChanges = true;
        }
        if (update.getTeamTwoGameThreeScore() != null &&
                !update.getTeamTwoGameThreeScore().equals(match.getTeamTwoGameThreeScore())) {
            match.setTeamTwoGameThreeScore(update.getTeamTwoGameThreeScore());
            hasChanges = true;
        }

        if (update.getTeamOneGameFourScore() != null &&
                !update.getTeamOneGameFourScore().equals(match.getTeamOneGameFourScore())) {
            match.setTeamOneGameFourScore(update.getTeamOneGameFourScore());
            hasChanges = true;
        }
        if (update.getTeamTwoGameFourScore() != null &&
                !update.getTeamTwoGameFourScore().equals(match.getTeamTwoGameFourScore())) {
            match.setTeamTwoGameFourScore(update.getTeamTwoGameFourScore());
            hasChanges = true;
        }

        if (update.getTeamOneGameFiveScore() != null &&
                !update.getTeamOneGameFiveScore().equals(match.getTeamOneGameFiveScore())) {
            match.setTeamOneGameFiveScore(update.getTeamOneGameFiveScore());
            hasChanges = true;
        }
        if (update.getTeamTwoGameFiveScore() != null &&
                !update.getTeamTwoGameFiveScore().equals(match.getTeamTwoGameFiveScore())) {
            match.setTeamTwoGameFiveScore(update.getTeamTwoGameFiveScore());
            hasChanges = true;
        }

        // Update match status
        if (update.getMatchStatus() != null &&
                !update.getMatchStatus().equals(match.getMatchStatus())) {
            match.setMatchStatus(update.getMatchStatus());
            hasChanges = true;
            log.info("Match status changed to: {}", update.getMatchStatus());
        }

        // Update winner
        if (update.getWinner() != null &&
                !update.getWinner().equals(match.getWinner())) {
            match.setWinner(update.getWinner());
            hasChanges = true;
            log.info("Match winner set to: {}", update.getWinner());
        }

        // Update server info
        if (update.getServer() != null) {
            match.setServer(update.getServer());
            hasChanges = true;
        }
        if (update.getServerFromTeam() != null) {
            match.setServerFromTeam(update.getServerFromTeam());
            hasChanges = true;
        }
        if (update.getCurrentServingNumber() != null) {
            match.setCurrentServingNumber(update.getCurrentServingNumber());
            hasChanges = true;
        }

        // Update game status
        if (update.getGameOneStatus() != null &&
                !update.getGameOneStatus().equals(match.getGameOneStatus())) {
            match.setGameOneStatus(update.getGameOneStatus());
            hasChanges = true;
        }
        if (update.getGameTwoStatus() != null &&
                !update.getGameTwoStatus().equals(match.getGameTwoStatus())) {
            match.setGameTwoStatus(update.getGameTwoStatus());
            hasChanges = true;
        }
        if (update.getGameThreeStatus() != null &&
                !update.getGameThreeStatus().equals(match.getGameThreeStatus())) {
            match.setGameThreeStatus(update.getGameThreeStatus());
            hasChanges = true;
        }

        // Update court if changed
        if (update.getCourtTitle() != null &&
                !update.getCourtTitle().isEmpty() &&
                !update.getCourtTitle().equals(match.getCourtTitle())) {
            match.setCourtTitle(update.getCourtTitle());
            hasChanges = true;
        }

        // Update timing if present
        if (update.getLocalDateMatchStart() != null) {
            LocalDateTime startTime = parseDateTime(update.getLocalDateMatchStart());
            if (startTime != null && !startTime.equals(match.getLocalDateMatchStart())) {
                match.setLocalDateMatchStart(startTime);
                hasChanges = true;
            }
        }

        if (update.getLocalDateMatchCompleted() != null) {
            LocalDateTime completedTime = parseDateTime(update.getLocalDateMatchCompleted());
            if (completedTime != null && !completedTime.equals(match.getLocalDateMatchCompleted())) {
                match.setLocalDateMatchCompleted(completedTime);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            match.setLastUpdate(LocalDateTime.now());
            matchRepository.save(match);

            log.info("Match {} updated - Game 1: {}-{}, Game 2: {}-{}, Game 3: {}-{}, Status: {}",
                    match.getMatchUuid(),
                    match.getTeamOneGameOneScore(), match.getTeamTwoGameOneScore(),
                    match.getTeamOneGameTwoScore(), match.getTeamTwoGameTwoScore(),
                    match.getTeamOneGameThreeScore(), match.getTeamTwoGameThreeScore(),
                    match.getMatchStatus()
            );

            // Broadcast update via WebSocket to connected clients
            try {
                messagingTemplate.convertAndSend(
                        "/topic/matches/" + update.getMatchUuid(),
                        update
                );
                log.debug("Broadcast update to WebSocket subscribers");
            } catch (Exception e) {
                log.error("Error broadcasting WebSocket message", e);
            }
        } else {
            log.debug("No changes detected for match {}", match.getMatchUuid());
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || dateTimeStr.equals("null")) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Could not parse datetime: {}", dateTimeStr);
            return null;
        }
    }
}
