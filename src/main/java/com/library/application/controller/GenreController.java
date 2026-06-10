package com.library.application.controller;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.payload.response.ApiResponse;
import com.library.application.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genre;

    @PostMapping
    public ResponseEntity<?> createGenre(@Valid @RequestBody GenreDTO genreDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genre.createGenre(genreDTO));
    }

    @PostMapping("/bulks")
    public ResponseEntity<?> createGenreBulk(@Valid @RequestBody List<GenreDTO> genreDTOs) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(genre.createGenresBulk(genreDTOs));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage(), false));
        }
    }
    @GetMapping("/{genreId}")
    public ResponseEntity<?> getGenreById(@Valid @PathVariable Long genreId) {
        return ResponseEntity.status(HttpStatus.OK).body(genre.getGenreById(genreId));
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<?> updateGenre(@PathVariable Long genreId, @Valid @RequestBody GenreDTO genreDTO) {
        return ResponseEntity.status(HttpStatus.OK).body(genre.updateGenre(genreId, genreDTO));
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long genreId) {
        genre.deleteGenre(genreId);
        ApiResponse apiResponse = new ApiResponse("genre deleted - soft delete", true);
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{genreId}/hard")
    public ResponseEntity<?> hardDeleteGenre(@Valid @PathVariable Long genreId) {
        genre.hardDeleteGenre(genreId);
        ApiResponse apiResponse = new ApiResponse("genre deleted - hard delete", true);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/top-level")
    public ResponseEntity<?> getTopLevel(){
        return ResponseEntity.status(HttpStatus.OK).body(genre.getTopLevelGenres());
    }

    @GetMapping("/count")
    public ResponseEntity<?> getGenreCount(){
        return ResponseEntity.ok(genre.getTotalActiveGenres());
    }

}
