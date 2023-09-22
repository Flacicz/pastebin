package com.example.pastebin.services;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.entity.Paste;
import com.example.pastebin.entity.PasteDetails;
import com.example.pastebin.entity.User;
import com.example.pastebin.mappers.PasteMapper;
import com.example.pastebin.repositories.PasteDetailsRepository;
import com.example.pastebin.repositories.UserRepository;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PasteService {
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final Bucket bucket = storage.get("pastebin1");

    private final UserRepository userRepository;
    private final PasteDetailsRepository pasteDetailsRepository;
    private final PasteMapper pasteMapper;

    public PasteService(UserRepository userRepository, PasteDetailsRepository pasteDetailsRepository, PasteMapper pasteMapper) {
        this.userRepository = userRepository;
        this.pasteDetailsRepository = pasteDetailsRepository;
        this.pasteMapper = pasteMapper;
    }

    public List<PasteDTO> findAll() {
        List<PasteDTO> pastes = new ArrayList<>();
        Page<Blob> blobs = bucket.list();

        for (Blob blob : blobs.getValues()) {
            byte[] bytes = blob.getContent();

            if (SerializationUtils.deserialize(bytes) == null) {
                throw new IllegalArgumentException("The byte[] must not be null");
            }
            pastes.add(pasteMapper.fromPasteToDTO((Paste) SerializationUtils.deserialize(bytes)));
        }

        return pastes;
    }

    public void connectUserToPaste(String username, Paste paste) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        } else {
            paste.setUser(user);
            user.getPastes().add(paste);
        }
    }

    public void saveText(PasteDTO pasteDTO, String username) {
        Paste paste = Paste.builder()
                .title(pasteDTO.getTitle())
                .text(pasteDTO.getText())
                .build();
        this.connectUserToPaste(username, paste);

        BlobId blobId = BlobId.of("pastebin1", "paste_" + paste.getId());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, SerializationUtils.serialize(paste));

        PasteDetails pasteDetails = PasteDetails.builder()
                .blobId(blobId)
                .bucket(blob.getBucket())
                .build();

        pasteDetailsRepository.save(pasteDetails);

        log.info("Saving new bin with title : {}", paste.getTitle());
    }

    public PasteDTO findPasteByTitle(String title) {
        Page<Blob> blobs = bucket.list();
        for (Blob blob : blobs.getValues()) {
            if (title.equals(blob.getName())) {
                PasteDTO pasteDTO = pasteMapper.fromPasteToDTO((Paste) SerializationUtils.deserialize(blob.getContent()));
            }
        }

        return null;
    }
}
