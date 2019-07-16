package com.ksj.cliapp.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {
	LogEntity findTopByOrderByIdDesc();
}
