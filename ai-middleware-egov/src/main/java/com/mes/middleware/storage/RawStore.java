package com.mes.middleware.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RawStore {
    private static final String RAW_DIR = "data/raw";

    public RawStored store(String payload) throws IOException {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String id = ts + "-" + UUID.randomUUID();
        Path dir = Paths.get(RAW_DIR);
        Files.createDirectories(dir);
        Path file = dir.resolve(id + ".raw");
        String safe = payload == null ? "" : payload;
        Files.writeString(file, safe, StandardCharsets.UTF_8);
        RawStored stored = new RawStored();
        stored.id = id;
        stored.size = safe.length();
        stored.storedAt = LocalDateTime.now().toString();
        return stored;
    }

    public static final class RawStored {
        public String id;
        public String storedAt;
        public int size;
    }
}
