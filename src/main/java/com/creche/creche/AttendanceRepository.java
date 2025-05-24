package com.creche.creche;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDateBetween(LocalDate startDate, LocalDate endDate);
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByChildParentId(Long parentId);
    void deleteByChildId(Long childId);
}