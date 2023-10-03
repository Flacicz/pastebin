package com.example.pastebin.hash_generator;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.nio.ByteBuffer;

@Component
@Slf4j
public class HashGenerator {
    private final EntityManager entityManager;

    public HashGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public int generateHashCode() {
        Session session = entityManager.unwrap(Session.class);

        Object sequenceValue = session.createQuery("SELECT nextval('hash_generator_seq')").uniqueResult();

        int hash = ByteBuffer.wrap(DigestUtils.sha256(SerializationUtils.serialize(sequenceValue))).getInt();

        log.info(String.valueOf(hash));

        return hash;
    }
}
