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
import java.util.*;

@Service
@Slf4j
public class PasteService {
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

    // helping functions
    public void connectUserToPaste(String username, PasteDetails pasteDetails) {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new RuntimeException("User not found");
        } else {
            user.getPastes().add(pasteDetails);
        }
    }

    public void editPasteInGCC(Paste paste, BlobId blobId) throws IOException {
        Blob blob = storage.get(blobId);

        WritableByteChannel channel = blob.writer();
        channel.write(ByteBuffer.wrap(Objects.requireNonNull(SerializationUtils.serialize(paste))));
        channel.close();
    }

    // searching functions
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

    public PasteDetails findPasteDetailsByHash(int hash) {
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

    public PasteDTO findPasteDTOByHash(int hash) {
        Blob blob = storage.get(this.findPasteDetailsByHash(hash).getBlobId());
        return pasteMapper.fromPasteToDTO((Paste) SerializationUtils.deserialize(blob.getContent()));
    }

    public Paste findPasteByHash(int hash) {
        Blob blob = storage.get(this.findPasteDetailsByHash(hash).getBlobId());
        return (Paste) SerializationUtils.deserialize(blob.getContent());
    }


    // functions working with pastes
    public void savePaste(PasteDTO pasteDTO, String username) {
        int hash = hashGenerator.generateHashCode();
        String title = pasteDTO.getTitle();

        Paste paste = Paste.builder()
                .id(pasteDetailsRepository.count() + 1)
                .hash(hash)
                .title(title)
                .text(pasteDTO.getText())
                .author(username)
                .views(0)
                .build();

        BlobId blobId = BlobId.of("pastebin1", title);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = storage.create(blobInfo, SerializationUtils.serialize(paste));

        PasteDetails pasteDetails = PasteDetails.builder()
                .hash(hash)
                .blobId(blobId)
                .bucket(blob.getBucket())
                .build();
        this.connectUserToPaste(username, pasteDetails);

        pasteDetailsRepository.save(pasteDetails);

        log.info("Saving new bin with title : {},Id1 = {},Id2 = {}", title, paste.getId(), pasteDetails.getId());
    }


    public void editPaste(PasteDTO pasteDTO) throws IOException {
        Paste paste = pasteMapper.fromDTOToPaste(pasteDTO);
        BlobId blobId = pasteDetailsRepository.findByHash(pasteDTO.getHash()).getBlobId();

        this.editPasteInGCC(paste, blobId);
    }

    public void deletePaste(int hash) {
        PasteDetails pasteDetails = findPasteDetailsByHash(hash);

        if (pasteDetails == null) {
            throw new RuntimeException("PasteDetails not found");
        } else {
            storage.delete(pasteDetails.getBlobId());
            pasteDetailsRepository.delete(pasteDetails);
        }
    }

    public void addView(PasteDTO pasteDTO) throws IOException {
        Paste paste = pasteMapper.fromDTOToPaste(pasteDTO);
        paste.setViews(paste.getViews() + 1);

        log.info(pasteDTO.getTitle());

        this.editPasteInGCC(paste, pasteDetailsRepository.findByHash(pasteDTO.getHash()).getBlobId());
    }

    public void addLike(String username, int hash) {
        PasteDetails pasteDetails = findPasteDetailsByHash(hash);
        Set<User> likes = pasteDetails.getLikes();
        User currUser = userRepository.findByUsername(username);

        if (likes.contains(currUser)) {
            likes.remove(currUser);
        } else {
            likes.add(currUser);
        }

        pasteDetailsRepository.save(pasteDetails);
    }

    public boolean meLikedCheck(String username, int hash) {
        User user = userRepository.findByUsername(username);
        PasteDetails pasteDetails = findPasteDetailsByHash(hash);

        return pasteDetails.getLikes().contains(user);
    }
}
