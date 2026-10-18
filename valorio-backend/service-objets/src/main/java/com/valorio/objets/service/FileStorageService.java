package com.valorio.objets.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le répertoire d'upload", e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalName = file.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID() + extension;
        try {
            Path targetPath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new IllegalStateException("Erreur lors de l'enregistrement du fichier", e);
        }
    }

    public void delete(String photoUrl) {
        if (photoUrl == null || !photoUrl.startsWith("/uploads/")) {
            return;
        }
        try {
            String filename = photoUrl.replace("/uploads/", "");
            Files.deleteIfExists(uploadDir.resolve(filename));
        } catch (IOException e) {
            // Ignorer silencieusement la suppression du fichier physique
        }
    }
}
