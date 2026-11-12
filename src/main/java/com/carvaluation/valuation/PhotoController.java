package com.carvaluation.valuation;

import com.carvaluation.auth.CustomUserDetails;
import com.carvaluation.storage.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.Locale;

@RestController
@RequestMapping("/valuation/photos")
public class PhotoController {

    private final ValuationService valuationService;
    private final FileStorageService storageService;

    public PhotoController(ValuationService valuationService, FileStorageService storageService) {
        this.valuationService = valuationService;
        this.storageService = storageService;
    }

    @GetMapping("/{photoId}/image")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long photoId,
                                           @AuthenticationPrincipal CustomUserDetails viewer) {
        if (viewer == null) return ResponseEntity.status(401).build();
        CarPhoto photo = valuationService.getPhotoForViewer(photoId, viewer.getUser()).orElse(null);
        if (photo == null) return ResponseEntity.notFound().build();

        String relativePath = photo.getFilePath();
        Path root = storageService.getRootLocation();
        Path resolved = root.resolve(relativePath).normalize();
        Path cars = root.resolve("cars").normalize();
        if (!resolved.startsWith(cars) || resolved.equals(cars)) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = imageType(relativePath);
        if (mediaType == null) return ResponseEntity.notFound().build();
        try {
            return ResponseEntity.ok().contentType(mediaType)
                    .header("X-Content-Type-Options", "nosniff")
                    .header("Cache-Control", "private, no-store")
                    .body(storageService.loadAsBytes(relativePath));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType imageType(String path) {
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".heic")) return MediaType.parseMediaType("image/heic");
        return null;
    }
}
