package com.tourneytea.service;

import com.tourneytea.dto.*;
import com.tourneytea.model.Tournament;
import com.tourneytea.model.Match;
import com.tourneytea.repository.TournamentRepository;
import com.tourneytea.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PickleballApiService {

    private final WebClient webClient;
    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://pickleballtournaments.com/api";

    public PickleballApiService(
            WebClient.Builder webClientBuilder,
            TournamentRepository tournamentRepository,
            MatchRepository matchRepository,
            ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl(BASE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer size
                .build();
        this.tournamentRepository = tournamentRepository;
        this.matchRepository = matchRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetch all PPA tournaments with pagination
     */
    public void fetchAndSaveAllPPATournaments() {
        log.info("Starting to fetch all PPA tournaments");

        int currentPage = 1;
        int totalSaved = 0;

        while (true) {
            try {
                TournamentResponse response = getPPATournaments(currentPage).block();

                if (response == null || response.getData() == null ||
                        response.getData().getItems().isEmpty()) {
                    log.info("No more tournaments found. Finished at page {}", currentPage);
                    break;
                }

                List<Tournament> tournaments = convertAndSaveTournaments(response.getData().getItems());
                totalSaved += tournaments.size();

                log.info("Saved {} tournaments from page {}", tournaments.size(), currentPage);

                // Check if we got fewer items than expected (indicates last page)
                if (response.getData().getItems().size() < response.getData().getTotalCount()) {
                    currentPage++;
                } else {
                    // If totalCount equals items size, we might be on the last page
                    break;
                }

            } catch (Exception e) {
                log.error("Error fetching tournaments at page {}", currentPage, e);
                break;
            }
        }

        log.info("Finished fetching tournaments. Total saved: {}", totalSaved);
    }

    /**
     * Fetch tournaments for a specific page
     */
    public Mono<TournamentResponse> getPPATournaments(int page) {
        log.debug("Fetching PPA tournaments page {}", page);
        return webClient.get()
                .uri("/getPPATournaments?currentPage={page}", page)
                .retrieve()
                .bodyToMono(TournamentResponse.class)
                .doOnError(e -> log.error("Error fetching tournaments", e));
    }

    /**
     * Fetch all matches for a tournament with pagination
     */
    public void fetchAndSaveAllMatches(String tournamentSlug, String tournamentId) {
        log.info("Starting to fetch all matches for tournament: {}", tournamentSlug);

        int currentPage = 1;
        int totalSaved = 0;

        while (true) {
            try {
                TickerResponse response = getTournamentMatches(tournamentSlug, currentPage).block();

                if (response == null || response.getData() == null ||
                        response.getData().getMatches().isEmpty()) {
                    log.info("No more matches found for {}. Finished at page {}", tournamentSlug, currentPage);
                    break;
                }

                List<Match> matches = convertAndSaveMatches(
                        response.getData().getMatches(),
                        tournamentId
                );
                totalSaved += matches.size();

                log.info("Saved {} matches from page {} for {}",
                        matches.size(), currentPage, tournamentSlug);

                currentPage++;

            } catch (Exception e) {
                log.error("Error fetching matches for tournament {} at page {}",
                        tournamentSlug, currentPage, e);
                break;
            }
        }

        log.info("Finished fetching matches for {}. Total saved: {}", tournamentSlug, totalSaved);
    }

    /**
     * Fetch matches for a specific page
     */
    public Mono<TickerResponse> getTournamentMatches(String tournamentSlug, int page) {
        log.debug("Fetching matches for tournament: {} page {}", tournamentSlug, page);
        return webClient.get()
                .uri("/v2/ticker?current_page={page}&tournament_slug={slug}&event_uuid=",
                        page, tournamentSlug)
                .retrieve()
                .bodyToMono(TickerResponse.class)
                .doOnError(e -> log.error("Error fetching matches for tournament: {}", tournamentSlug, e));
    }

    /**
     * Convert API tournament items to domain models and save
     */
    private List<Tournament> convertAndSaveTournaments(List<TournamentItem> items) {
        List<Tournament> tournaments = new ArrayList<>();

        for (TournamentItem item : items) {
            try {
                Tournament tournament = new Tournament();
                tournament.setId(item.getId());
                tournament.setTitle(item.getTitle());
                tournament.setSlug(item.getSlug());
                tournament.setDateFrom(parseDateTime(item.getDateFrom()));
                tournament.setDateTo(parseDateTime(item.getDateTo()));
                tournament.setLocation(item.getLocation());
                tournament.setStatus(item.getStatus());
                tournament.setCurrency(item.getCurrency());
                tournament.setIsCanceled(item.getIsCanceled());
                tournament.setIsRegistrationClosed(item.getIsRegistrationClosed());
                tournament.setIsTournamentCompleted(item.getIsTournamentCompleted());
                tournament.setIsPrizeMoney(item.getIsPrizeMoney());
                tournament.setLat(item.getLat());
                tournament.setLng(item.getLng());
                tournament.setLogo(item.getLogo());
                tournament.setPrice(item.getPrice());
                tournament.setRegistrationCount(item.getRegistrationCount());
                tournament.setRawData(objectMapper.writeValueAsString(item));

                tournaments.add(tournament);
            } catch (Exception e) {
                log.error("Error converting tournament {}", item.getId(), e);
            }
        }

        return tournamentRepository.saveAll(tournaments);
    }

    /**
     * Convert API match matchDatas to domain models and save
     */
    private List<Match> convertAndSaveMatches(List<MatchData> matchDataList, String tournamentId) {
        List<Match> matches = new ArrayList<>();

        for (MatchData matchData : matchDataList) {
            try {
                Match match = new Match();
                match.setMatchUuid(matchData.getMatchUuid());
                match.setTournamentId(tournamentId);
                match.setEventUuid(matchData.getEventUuid());
                match.setEventTitle(matchData.getEventTitle());
                match.setRoundText(matchData.getRoundText());
                match.setRoundNumber(matchData.getRoundNumber());
                match.setCourtTitle(matchData.getCourtTitle());

                // Team 1 Players
                match.setTeamOnePlayerOneUuid(matchData.getTeamOnePlayerOneUuid());
                match.setTeamOnePlayerOneName(
                        buildPlayerName(matchData.getTeamOnePlayerOneFirstName(), matchData.getTeamOnePlayerOneLastName())
                );
                match.setTeamOnePlayerTwoUuid(matchData.getTeamOnePlayerTwoUuid());
                match.setTeamOnePlayerTwoName(
                        buildPlayerName(matchData.getTeamOnePlayerTwoFirstName(), matchData.getTeamOnePlayerTwoLastName())
                );

                // Team 2 Players
                match.setTeamTwoPlayerOneUuid(matchData.getTeamTwoPlayerOneUuid());
                match.setTeamTwoPlayerOneName(
                        buildPlayerName(matchData.getTeamTwoPlayerOneFirstName(), matchData.getTeamTwoPlayerOneLastName())
                );
                match.setTeamTwoPlayerTwoUuid(matchData.getTeamTwoPlayerTwoUuid());
                match.setTeamTwoPlayerTwoName(
                        buildPlayerName(matchData.getTeamTwoPlayerTwoFirstName(), matchData.getTeamTwoPlayerTwoLastName())
                );

                // Scores
                match.setTeamOneGameOneScore(matchData.getTeamOneGameOneScore());
                match.setTeamTwoGameOneScore(matchData.getTeamTwoGameOneScore());
                match.setTeamOneGameTwoScore(matchData.getTeamOneGameTwoScore());
                match.setTeamTwoGameTwoScore(matchData.getTeamTwoGameTwoScore());
                match.setTeamOneGameThreeScore(matchData.getTeamOneGameThreeScore());
                match.setTeamTwoGameThreeScore(matchData.getTeamTwoGameThreeScore());
                match.setTeamOneGameFourScore(matchData.getTeamOneGameFourScore());
                match.setTeamTwoGameFourScore(matchData.getTeamTwoGameFourScore());
                match.setTeamOneGameFiveScore(matchData.getTeamOneGameFiveScore());
                match.setTeamTwoGameFiveScore(matchData.getTeamTwoGameFiveScore());

                // Match Status
                match.setMatchStatus(matchData.getMatchStatus());
                match.setMatchCompletedType(matchData.getMatchCompletedType());
                match.setWinner(matchData.getWinner());
                match.setTeamOneWinningPercentage(matchData.getTeamOneWinningPercentage());

                // Game Status
                match.setGameOneStatus(matchData.getGameOneStatus());
                match.setGameTwoStatus(matchData.getGameTwoStatus());
                match.setGameThreeStatus(matchData.getGameThreeStatus());

                // Server Info
                match.setServer(matchData.getServer());
                match.setServerFromTeam(matchData.getServerFromTeam());
                match.setCurrentServingNumber(matchData.getCurrentServingNumber());

                // Timing
                match.setLocalDateMatchStart(parseDateTime(matchData.getLocalDateMatchStart()));
                match.setLocalDateMatchPlannedStart(parseDateTime(matchData.getLocalDateMatchPlannedStart()));
                match.setLocalDateMatchCompleted(parseDateTime(matchData.getLocalDateMatchCompleted()));
                match.setLocalDateMatchAssignedToCourt(parseDateTime(matchData.getLocalDateMatchAssignedToCourt()));

                match.setLastUpdate(LocalDateTime.now());
                match.setRawData(objectMapper.writeValueAsString(matchData));

                matches.add(match);
            } catch (Exception e) {
                log.error("Error converting match {}", matchData.getMatchUuid(), e);
            }
        }

        return matchRepository.saveAll(matches);
    }

    private String buildPlayerName(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) {
            return "";
        }
        return (firstName + " " + (lastName != null ? lastName : "")).trim();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
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
