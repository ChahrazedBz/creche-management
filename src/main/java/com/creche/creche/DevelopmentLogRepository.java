package com.creche.creche;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DevelopmentLogRepository extends JpaRepository<DevelopmentLog, Long> {
    List<DevelopmentLog> findByChildId(Long childId);
    List<DevelopmentLog> findByDate(LocalDate date);
}