package com.example.pastebin.entity;

import com.google.cloud.storage.BlobId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(
            name = "paste_likes",
            joinColumns = {@JoinColumn(name = "paste_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<User> likes = new HashSet<>();

    private LocalDateTime dateOfCreated;

    @PrePersist
    private void init() {
        this.dateOfCreated = LocalDateTime.now();
    }
}
