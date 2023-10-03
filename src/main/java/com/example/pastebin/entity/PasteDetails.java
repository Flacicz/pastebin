package com.example.pastebin.entity;

import com.google.cloud.storage.BlobId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasteDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "hash")
    private int hash;

    @Column(name = "blobId")
    private BlobId blobId;

    @Column(name = "bucket")
    private String bucket;

    @ManyToOne()
    private User user;

    private LocalDateTime dateOfCreated;

    @PrePersist
    private void init() {
        this.dateOfCreated = LocalDateTime.now();
    }
}
