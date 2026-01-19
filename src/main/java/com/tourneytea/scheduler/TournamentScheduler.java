package com.tourneytea.scheduler;

import com.tourneytea.service.*;
import com.tourneytea.repository.*;
import com.tourneytea.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TournamentScheduler {

    private final PickleballApiService apiService;
    private final LiveScoreStreamService liveScoreService;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;

    // Run every 30 minutes to fetch tournaments
    @Scheduled(fixedRate = 1800000, initialDelay = 5000)
    public void fetchActiveTournaments() {
        log.info("Scheduled task: Fetching PPA tournaments...");

        try {
            apiService.fetchAndSaveAllPPATournaments();

            // After fetching tournaments, fetch matches for active ones
            fetchMatchesForActiveTournaments();
        } catch (Exception e) {
            log.error("Error in scheduled tournament fetch", e);
        }
    }

    // Run every 2 minutes to fetch matches for active tournaments
    @Scheduled(fixedRate = 120000, initialDelay = 15000)
    public void fetchMatchesForActiveTournaments() {
        log.info("Scheduled task: Fetching matches for active tournaments...");

        try {
            List<Tournament> activeTournaments = tournamentRepository.findActiveTournaments();
            log.info("Found {} active tournaments", activeTournaments.size());

            for (Tournament tournament : activeTournaments) {
                log.info("Fetching matches for tournament: {}", tournament.getTitle());
                apiService.fetchAndSaveAllMatches(tournament.getSlug(), tournament.getId());
            }
        } catch (Exception e) {
            log.error("Error fetching matches for active tournaments", e);
        }
    }

    // Run every 30 seconds to check for live matches and connect to stream
    @Scheduled(fixedRate = 30000, initialDelay = 20000)
    public void connectToLiveMatches() {
        log.info("Scheduled task: Checking for live matches...");

        try {
            List<Match> liveMatches = matchRepository.findLiveMatches();

            if (!liveMatches.isEmpty()) {
                List<String> matchIds = liveMatches.stream()
                        .map(Match::getMatchUuid)
                        .collect(Collectors.toList());

                log.info("Found {} live matches, connecting to stream...", matchIds.size());
                liveScoreService.connectToLiveScores(matchIds);
            } else {
                log.info("No live matches found");
            }
        } catch (Exception e) {
            log.error("Error connecting to live matches", e);
        }
    }
}
