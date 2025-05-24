package com.creche.creche;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    void deleteByChildId(Long childId);
    List<Inscription> findByStatus(String status);
    List<Inscription> findByChildId(Long childId);
}