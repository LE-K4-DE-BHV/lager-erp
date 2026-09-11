package com.lagermanagement.space.service;

import com.lagermanagement.space.config.AppFilesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final AppFilesProperties filesProperties;

    /**
     * Listet alle regulären Dateien im angegebenen Verzeichnis, die dem Glob-Pattern entsprechen.
     * Beispiel-Pattern: "*.csv", "*.xlsx", "*.{csv,xlsx}"
     */
    public List<Path> scanDirectory(Path dir, String pattern) {
        if (!Files.isDirectory(dir)) {
            log.warn("Verzeichnis existiert nicht oder ist kein Ordner: {}", dir.getFileName());
            return List.of();
        }
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir, pattern)) {
            return StreamSupport.stream(dirStream.spliterator(), false)
                    .filter(Files::isRegularFile)
                    .toList();
        } catch (IOException e) {
            log.warn("Verzeichnis konnte nicht gescannt werden: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Berechnet den SHA-256-Hash einer Datei und gibt ihn als 64-stelligen Hex-String zurück.
     */
    public String computeHash(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            byte[] hash = digest.digest(bytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nicht verfügbar", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Datei konnte nicht gelesen werden: " + file.getFileName(), e);
        }
    }

    /**
     * Verschiebt eine Datei in targetDir/YYYY-MM-DD/dateiname.
     * Der Unterordner wird automatisch erstellt.
     * Es werden nur Dateinamen geloggt, keine absoluten Pfade.
     */
    public void moveFile(Path source, Path targetDir) {
        String datumOrdner = LocalDate.now().toString();
        Path zielOrdner = targetDir.resolve(datumOrdner);
        try {
            Files.createDirectories(zielOrdner);
            Path ziel = zielOrdner.resolve(source.getFileName());
            Files.move(source, ziel, StandardCopyOption.REPLACE_EXISTING);
            log.info("Datei verschoben: {} → {}/{}", source.getFileName(), datumOrdner, source.getFileName());
        } catch (IOException e) {
            log.error("Datei konnte nicht verschoben werden: {} – {}", source.getFileName(), e.getMessage());
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Kombiniert app.files.base-path mit einem relativen Unterpfad.
     */
    public Path resolveOutputPath(String subPath) {
        return Path.of(filesProperties.getBasePath()).resolve(subPath);
    }
}
