package com.example.pastebin.repositories;

import com.example.pastebin.entity.PasteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasteDetailsRepository extends JpaRepository<PasteDetails, Long> {
    PasteDetails findByHash(int hash);
}
