package com.familyhub.digital_family_hub.albums;

import com.familyhub.digital_family_hub.shared.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @GetMapping
    public ApiResponse<List<AlbumDTO.Response>> listAlbums(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) AlbumCategory category,
        @RequestParam(required = false) UUID createdById
    ) {
        return ApiResponse.ok(albumService.listAlbums(page, size, category, createdById));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AlbumDTO.Response> createAlbum(@Valid @RequestBody AlbumDTO.Request request) {
        return ApiResponse.created(albumService.createAlbum(request));
    }

    @PutMapping("/{albumId}")
    public ApiResponse<AlbumDTO.Response> updateAlbum(
        @PathVariable UUID albumId,
        @Valid @RequestBody AlbumDTO.Request request
    ) {
        return ApiResponse.ok(albumService.updateAlbum(albumId, request));
    }

    @GetMapping("/{albumId}/media")
    public ApiResponse<List<AlbumDTO.MediaResponse>> listAlbumMedia(@PathVariable UUID albumId) {
        return ApiResponse.ok(albumService.listAlbumMedia(albumId));
    }

    @PostMapping("/{albumId}/media")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AlbumDTO.MediaResponse> attachMedia(
        @PathVariable UUID albumId,
        @Valid @RequestBody AlbumDTO.AttachMediaRequest request
    ) {
        return ApiResponse.created(albumService.attachMedia(albumId, request));
    }
}
