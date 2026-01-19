package com.tourneytea.repository;

import com.tourneytea.model.Tournament;
import com.tourneytea.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, String> {
    List<Tournament> findByIsTournamentCompleted(Boolean isCompleted);

    @Query("SELECT t FROM Tournament t WHERE t.isTournamentCompleted = false " +
            "OR (t.dateFrom <= CURRENT_TIMESTAMP AND t.dateTo >= CURRENT_TIMESTAMP) " +
            "ORDER BY t.dateFrom ASC")
    List<Tournament> findActiveTournaments();

    @Query("SELECT t FROM Tournament t WHERE " +
            "t.dateFrom <= CURRENT_TIMESTAMP AND t.dateTo >= CURRENT_TIMESTAMP " +
            "ORDER BY t.dateFrom ASC")
    List<Tournament> findCurrentlyRunningTournaments();
}
