package com.tourneytea.service;

import com.tourneytea.model.LiveScoreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LiveScoreStreamService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MatchUpdateService matchUpdateService;

    private static final String LIVE_SCORE_URL = "https://rte.pbgql.co";

    // Track active SSE connections
    private final Map<String, Disposable> activeConnections = new ConcurrentHashMap<>();
    private String currentConnectionKey = null;

    public LiveScoreStreamService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            MatchUpdateService matchUpdateService) {
        this.objectMapper = objectMapper;
        this.matchUpdateService = matchUpdateService;
        this.webClient = webClientBuilder
                .baseUrl(LIVE_SCORE_URL)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public void connectToLiveScores(List<String> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            log.warn("No match IDs provided for live score connection");
            return;
        }

        // Create a key for this set of matches
        String connectionKey = String.join(",", matchIds);

        // If we're already connected to these same matches, don't reconnect
        if (connectionKey.equals(currentConnectionKey) &&
                activeConnections.containsKey(connectionKey)) {
            log.debug("Already connected to these matches, skipping reconnection");
            return;
        }

        // Disconnect previous connections
        disconnectAll();

        log.info("Connecting to live scores for {} matches: {}", matchIds.size(), matchIds);

        String fingerprint = generateFingerprint();
        String token = generateToken(fingerprint);
        String matchesHeader = encodeMatchIds(matchIds);

        log.debug("Generated token: {}", token);
        log.debug("Matches header: {}", matchesHeader);

        try {
            Disposable subscription = webClient.get()
                    .uri("/live-scoring")
                    .header("Accept", "text/event-stream")
                    .header("PB-RTE-TOKEN", token)
                    .header("X-Request-Matches", matchesHeader)
                    .header("Origin", "https://pickleballtournaments.com")
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnSubscribe(s -> log.info("SSE stream subscribed"))
                    .doOnNext(this::processSSEMessage)
                    .doOnError(error -> {
                        log.error("Error in live score stream", error);
                        activeConnections.remove(connectionKey);
                        currentConnectionKey = null;
                    })
                    .doOnComplete(() -> {
                        log.info("Live score stream completed");
                        activeConnections.remove(connectionKey);
                        currentConnectionKey = null;
                    })
                    .subscribe();

            activeConnections.put(connectionKey, subscription);
            currentConnectionKey = connectionKey;

            log.info("Successfully subscribed to live score stream");

        } catch (Exception e) {
            log.error("Failed to connect to live scores", e);
        }
    }

    private void disconnectAll() {
        if (!activeConnections.isEmpty()) {
            log.info("Disconnecting {} existing SSE connections", activeConnections.size());
            activeConnections.values().forEach(Disposable::dispose);
            activeConnections.clear();
            currentConnectionKey = null;
        }
    }

    private void processSSEMessage(String message) {
        try {
            // Log raw message for debugging
            log.debug("Raw SSE message: {}", message);

            // Skip keepalive and init messages
            if (message.startsWith(":")) {
                log.debug("Server message: {}", message);
                return;
            }

            String eventType = "message";
            String data = null;

            // Check if message has SSE formatting (event:, data:)
            if (message.contains("event:") || message.contains("data:")) {
                // Parse SSE format - messages can be multi-line
                String[] lines = message.split("\n");

                for (String line : lines) {
                    if (line.startsWith("event:")) {
                        eventType = line.substring(6).trim();
                    } else if (line.startsWith("data:")) {
                        data = line.substring(5).trim();
                    }
                }
            } else {
                // Raw JSON without SSE formatting
                data = message.trim();
            }

            if (data != null && !data.isEmpty()) {
                try {
                    Map<String, Object> eventData = objectMapper.readValue(data, Map.class);
                    LiveScoreUpdate update = parseScoreUpdate(eventData);
                    update.setEventType(eventType);

                    log.info("Received score update for match: {}", update.getMatchUuid());
                    matchUpdateService.processUpdate(update);

                } catch (Exception e) {
                    log.error("Error parsing JSON data", e);
                    log.debug("Failed to parse: {}", data);
                }
            }
        } catch (Exception e) {
            log.error("Error processing SSE message: {}", message, e);
        }
    }

    private LiveScoreUpdate parseScoreUpdate(Map<String, Object> data) {
        LiveScoreUpdate update = new LiveScoreUpdate();

        // Match UUID
        update.setMatchUuid((String) data.get("matchUuid"));

        // Server info
        update.setServer(getInteger(data, "server"));
        update.setServerFromTeam(getInteger(data, "serverFromTeam"));
        update.setCurrentServingNumber(getInteger(data, "currentServingNumber"));

        // Match status
        update.setMatchStatus(getInteger(data, "matchStatus"));
        update.setMatchCompletedType(getInteger(data, "matchCompletedType"));
        update.setWinner(getInteger(data, "winner"));
        update.setCurrentGame(getInteger(data, "currentGame"));

        // Game scores - Team One
        update.setTeamOneGameOneScore(getInteger(data, "teamOneGameOneScore"));
        update.setTeamOneGameTwoScore(getInteger(data, "teamOneGameTwoScore"));
        update.setTeamOneGameThreeScore(getInteger(data, "teamOneGameThreeScore"));
        update.setTeamOneGameFourScore(getInteger(data, "teamOneGameFourScore"));
        update.setTeamOneGameFiveScore(getInteger(data, "teamOneGameFiveScore"));

        // Game scores - Team Two
        update.setTeamTwoGameOneScore(getInteger(data, "teamTwoGameOneScore"));
        update.setTeamTwoGameTwoScore(getInteger(data, "teamTwoGameTwoScore"));
        update.setTeamTwoGameThreeScore(getInteger(data, "teamTwoGameThreeScore"));
        update.setTeamTwoGameFourScore(getInteger(data, "teamTwoGameFourScore"));
        update.setTeamTwoGameFiveScore(getInteger(data, "teamTwoGameFiveScore"));

        // Game status
        update.setGameOneStatus(getString(data, "gameOneStatus"));
        update.setGameTwoStatus(getString(data, "gameTwoStatus"));
        update.setGameThreeStatus(getString(data, "gameThreeStatus"));
        update.setGameFourStatus(getString(data, "gameFourStatus"));
        update.setGameFiveStatus(getString(data, "gameFiveStatus"));

        // Court info
        update.setCourtUuid(getString(data, "courtUuid"));
        update.setCourtTitle(getString(data, "courtTitle"));

        // Player names
        update.setTeamOnePlayerOneFirstName(getString(data, "teamOnePlayerOneFirstName"));
        update.setTeamOnePlayerOneLastName(getString(data, "teamOnePlayerOneLastName"));
        update.setTeamOnePlayerTwoFirstName(getString(data, "teamOnePlayerTwoFirstName"));
        update.setTeamOnePlayerTwoLastName(getString(data, "teamOnePlayerTwoLastName"));

        update.setTeamTwoPlayerOneFirstName(getString(data, "teamTwoPlayerOneFirstName"));
        update.setTeamTwoPlayerOneLastName(getString(data, "teamTwoPlayerOneLastName"));
        update.setTeamTwoPlayerTwoFirstName(getString(data, "teamTwoPlayerTwoFirstName"));
        update.setTeamTwoPlayerTwoLastName(getString(data, "teamTwoPlayerTwoLastName"));

        // Timing
        update.setLocalDateMatchStart(getString(data, "localDateMatchStart"));
        update.setLocalDateMatchCompleted(getString(data, "localDateMatchCompleted"));
        update.setLocalDateMatchPlannedStart(getString(data, "localDateMatchPlannedStart"));
        update.setLocalDateMatchAssignedToCourt(getString(data, "localDateMatchAssignedToCourt"));

        // Metadata
        update.setTimestamp(java.time.LocalDateTime.now());
        update.setRawData(data);

        return update;
    }

    private Integer getInteger(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        String str = value.toString();
        return str.isEmpty() ? null : str;
    }

    private String generateFingerprint() {
        String data = String.format("%d-%s",
                System.currentTimeMillis(),
                UUID.randomUUID().toString()
        );
        return Integer.toHexString(data.hashCode());
    }

    private String generateToken(String fingerprint) {
        try {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("ua", "PickleballTracker/1.0");
            tokenData.put("origin", "https://pickleballtournaments.com");
            tokenData.put("fingerprint", fingerprint);

            String json = objectMapper.writeValueAsString(tokenData);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error generating token", e);
            return "";
        }
    }

    private String encodeMatchIds(List<String> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return "";
        }
        String joined = String.join(",", matchIds);
        return Base64.getEncoder().encodeToString(joined.getBytes(StandardCharsets.UTF_8));
    }
}

