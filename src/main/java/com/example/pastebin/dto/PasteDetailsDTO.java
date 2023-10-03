package com.example.pastebin.dto;

import com.google.cloud.storage.BlobId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasteDetailsDTO {
    private Long id;
    private BlobId blobId;
    private String bucket;
    private int hash;
}
