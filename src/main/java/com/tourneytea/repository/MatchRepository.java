package com.tourneytea.repository;

import com.tourneytea.model.Tournament;
import com.tourneytea.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, String> {
    List<Match> findByTournamentId(String tournamentId);
    List<Match> findByMatchStatus(Integer status);

    @Query("SELECT m FROM Match m WHERE m.matchStatus = 2")
    List<Match> findLiveMatches();

    @Query("SELECT m FROM Match m WHERE m.winner > 0")
    List<Match> findCompletedMatches();
}