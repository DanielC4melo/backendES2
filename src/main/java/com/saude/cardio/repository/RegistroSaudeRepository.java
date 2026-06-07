package com.saude.cardio.repository;

import com.saude.cardio.model.RegistroSaude;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroSaudeRepository extends JpaRepository<RegistroSaude, Long> {
}
