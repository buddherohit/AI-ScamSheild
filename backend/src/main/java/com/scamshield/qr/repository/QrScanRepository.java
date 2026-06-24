package com.scamshield.qr.repository;

import com.scamshield.qr.entity.QrScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QrScanRepository extends JpaRepository<QrScan, Long> {
    List<QrScan> findByUserIdOrderByCreatedAtDesc(Long userId);
}
