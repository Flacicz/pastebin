package com.example.pastebin.services;

import com.example.pastebin.dto.PasteDTO;
import com.example.pastebin.entity.Paste;
import com.example.pastebin.entity.PasteDetails;
import com.example.pastebin.entity.User;
import com.example.pastebin.hash_generator.HashGenerator;
import com.example.pastebin.mappers.PasteMapper;
import com.example.pastebin.repositories.PasteDetailsRepository;
import com.example.pastebin.repositories.UserRepository;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PasteService {
    private String oldTitle;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final Bucket bucket = storage.get("pastebin1");

    private final UserRepository userRepository;
    private final PasteDetailsRepository pasteDetailsRepository;
    private final PasteMapper pasteMapper;
    private final HashGenerator hashGenerator;

    public PasteService(UserRepository userRepository, PasteDetailsRepository pasteDetailsRepository, PasteMapper pasteMapper, HashGenerator hashGenerator) {
        this.userRepository = userRepository;
        this.pasteDetailsRepository = pasteDetailsRepository;
        this.pasteMapper = pasteMapper;
        this.hashGenerator = hashGenerator;
    }

    public void saveOldTitle(String title) {
        oldTitle = title;
    }

    public void connectUserToPaste(String username, PasteDetails pasteDetails) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        } else {
            pasteDetails.setUser(user);
            user.getPastes().add(pasteDetails);
        }
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

    public void saveText(PasteDTO pasteDTO, String username) {
        int hash = hashGenerator.generateHashCode();

        Paste paste = Paste.builder()
                .id(pasteDetailsRepository.count() + 1)
                .hash(hash)
                .title(pasteDTO.getTitle())
                .text(pasteDTO.getText())
                .author(username)
                .build();

        BlobId blobId = BlobId.of("pastebin1", paste.getTitle());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, SerializationUtils.serialize(paste));

        PasteDetails pasteDetails = PasteDetails.builder()
                .hash(hash)
                .blobId(blobId)
                .bucket(blob.getBucket())
                .build();
        this.connectUserToPaste(username, pasteDetails);

        pasteDetailsRepository.save(pasteDetails);

        log.info("Saving new bin with title : {},Id1 = {},Id2 = {}", paste.getTitle(), paste.getId(), pasteDetails.getId());
    }

    public PasteDetails findPasteDetailsByHash(int hash) {
        log.info(String.valueOf(hash));

        if (pasteDetailsRepository.findByHash(hash) == null) {
            throw new RuntimeException("Paste not found");
        }

        Optional<PasteDetails> optionalPasteDetails = Optional.ofNullable(pasteDetailsRepository.findByHash(hash));
        PasteDetails pasteDetails = new PasteDetails();

        if (optionalPasteDetails.isPresent()) {
            pasteDetails = optionalPasteDetails.get();
        }

        return pasteDetails;
    }

    public PasteDTO findPasteByHash(int hash) {
        Blob blob = storage.get(this.findPasteDetailsByHash(hash).getBlobId());
        return pasteMapper.fromPasteToDTO((Paste) SerializationUtils.deserialize(blob.getContent()));
    }

    public void editPaste(PasteDTO pasteDTO) throws IOException {
        log.info(String.valueOf(pasteDTO.getHash()));
        Paste paste = pasteMapper.fromDTOToPaste(pasteDTO);

        BlobId blobId = BlobId.of("pastebin1", oldTitle);
        Blob blob = storage.get(blobId);

        WritableByteChannel channel = blob.writer();
        channel.write(ByteBuffer.wrap(Objects.requireNonNull(SerializationUtils.serialize(paste))));
        channel.close();
    }

    public void deletePaste(int hash) {
        PasteDetails pasteDetails = this.findPasteDetailsByHash(hash);

        if (pasteDetails == null) {
            throw new RuntimeException("PasteDetails not found");
        } else {
            storage.delete(pasteDetails.getBlobId());
            pasteDetailsRepository.deleteById(pasteDetails.getId());
        }
    }
}
