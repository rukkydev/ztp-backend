package com.ztp.account;

import com.ztp.user.User;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String uploadAvatar(Long userId, MultipartFile file) {
    if (file.isEmpty()) {
        throw new IllegalArgumentException("No file provided");
    }

    byte[] fileBytes;
    try {
        fileBytes = file.getBytes();
    } catch (IOException e) {
        throw new IllegalStateException("Failed to read uploaded file", e);
    }

    if (!matchesRealImageType(fileBytes)) {
        throw new IllegalArgumentException("File does not appear to be a valid JPEG, PNG, or WebP image");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    try {
        Path dir = Path.of(uploadDir);
        Files.createDirectories(dir);

        String extension = extractExtension(file.getOriginalFilename());
        String filename = "user-" + userId + "-" + UUID.randomUUID() + extension;
        Path target = dir.resolve(filename);

        Files.write(target, fileBytes); // reuse the bytes we already read, avoid re-reading the stream

        deleteOldAvatarIfPresent(user);

        String url = "/uploads/avatars/" + filename;
        user.setAvatarUrl(url);
        userRepository.save(user);

        return url;
    } catch (IOException e) {
        throw new IllegalStateException("Failed to store avatar file", e);
    }
}


    private void deleteOldAvatarIfPresent(User user) {
        if (user.getAvatarUrl() == null) return;
        try {
            String oldFilename = user.getAvatarUrl().substring(user.getAvatarUrl().lastIndexOf('/') + 1);
            Files.deleteIfExists(Path.of(uploadDir, oldFilename));
        } catch (IOException ignored) {
            // Best-effort cleanup -- not worth failing the upload over.
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
	
	private static final java.util.Map<String, byte[]> MAGIC_NUMBERS = java.util.Map.of(
        "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46} // "RIFF" -- WebP header starts this way
);

private boolean matchesRealImageType(byte[] fileBytes) {
    for (byte[] signature : MAGIC_NUMBERS.values()) {
        if (fileBytes.length < signature.length) continue;
        boolean matches = true;
        for (int i = 0; i < signature.length; i++) {
            if (fileBytes[i] != signature[i]) {
                matches = false;
                break;
            }
        }
        if (matches) return true;
    }
    return false;
}
}