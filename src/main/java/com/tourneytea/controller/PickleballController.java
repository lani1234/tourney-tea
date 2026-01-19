package com.tourneytea.controller;

import com.tourneytea.model.*;
import com.tourneytea.repository.*;
import com.tourneytea.service.LiveScoreStreamService;
import com.tourneytea.service.PickleballApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PickleballController {

    private final TournamentRepository tournamentRepository;
    private final MatchRepository matchRepository;
    private final PickleballApiService apiService;
    private final LiveScoreStreamService liveScoreService;

    @GetMapping("/tournaments")
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    @GetMapping("/tournaments/active")
    public List<Tournament> getActiveTournaments() {
        return tournamentRepository.findActiveTournaments();
    }

    @GetMapping("/tournaments/{id}")
    public ResponseEntity<Tournament> getTournament(@PathVariable String id) {
        return tournamentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tournaments/{id}/matches")
    public List<Match> getTournamentMatches(@PathVariable String id) {
        return matchRepository.findByTournamentId(id);
    }

    @GetMapping("/matches/live")
    public List<Match> getLiveMatches() {
        return matchRepository.findLiveMatches();
    }

    @GetMapping("/matches/completed")
    public List<Match> getCompletedMatches() {
        return matchRepository.findCompletedMatches();
    }

    @GetMapping("/matches/{id}")
    public ResponseEntity<Match> getMatch(@PathVariable String id) {
        return matchRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Debug/Admin endpoints
    @PostMapping("/admin/fetch-tournaments")
    public ResponseEntity<String> fetchTournaments() {
        try {
            apiService.fetchAndSaveAllPPATournaments();
            return ResponseEntity.ok("Tournament fetch triggered");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/admin/fetch-matches/{tournamentSlug}")
    public ResponseEntity<String> fetchMatches(
            @PathVariable String tournamentSlug,
            @RequestParam String tournamentId) {
        try {
            apiService.fetchAndSaveAllMatches(tournamentSlug, tournamentId);
            return ResponseEntity.ok("Match fetch triggered for " + tournamentSlug);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/admin/connect-live-scores")
    public ResponseEntity<String> connectLiveScores() {
        try {
            List<Match> liveMatches = matchRepository.findLiveMatches();
            if (liveMatches.isEmpty()) {
                return ResponseEntity.ok("No live matches found");
            }

            List<String> matchIds = liveMatches.stream()
                    .map(Match::getMatchUuid)
                    .collect(java.util.stream.Collectors.toList());

            liveScoreService.connectToLiveScores(matchIds);
            return ResponseEntity.ok("Connected to " + matchIds.size() + " live matches");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTournaments", tournamentRepository.count());
        stats.put("activeTournaments", tournamentRepository.findActiveTournaments().size());
        stats.put("totalMatches", matchRepository.count());
        stats.put("liveMatches", matchRepository.findLiveMatches().size());
        stats.put("completedMatches", matchRepository.findCompletedMatches().size());
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/admin/add-tournament-manually")
    public ResponseEntity<String> addTournamentManually(
            @RequestParam String tournamentId,
            @RequestParam String tournamentSlug) {
        try {
            apiService.fetchAndSaveAllMatches(tournamentSlug, tournamentId);
            return ResponseEntity.ok("Added tournament and fetched matches for: " + tournamentSlug);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}