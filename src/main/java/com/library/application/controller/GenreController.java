package com.library.application.controller;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.payload.response.ApiResponse;
import com.library.application.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genre;

    @PostMapping("/create")
    public ResponseEntity<?> createGenre(@Valid @RequestBody GenreDTO genreDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genre.createGenre(genreDTO));
    }
    @PostMapping("/bulks")
    public ResponseEntity<?> createGenreBulk(@Valid @RequestBody List<GenreDTO> genreDTOs) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genre.createGenresBulk(genreDTOs));
    }
    @GetMapping("/{genreId}")
    public ResponseEntity<?> getGenreById( @PathVariable Long genreId) {
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

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getGenreByCode(@PathVariable String code){
        return ResponseEntity.ok(genre.getGenreByCode(code));
    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveGenre(){
        return ResponseEntity.status(HttpStatus.OK).body(genre.getAllActiveGenres());
    }
    @GetMapping("/active/hierarchy")
    public ResponseEntity<?> getAllActiveSubGenres(){
        return ResponseEntity.ok(genre.getAllActiveGenresWithSubGenres());

    }

    /*
    * @TODO LETTER ON
    @GetMapping
    public ResponseEntity<?> searchGenres(@RequestParam(required = false) @Valid String searchTerm, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(genre.searchGenres(searchTerm, pageable));
    }*/

    @GetMapping("/sub-genres/{parentId}")
    public ResponseEntity<?> getSubGenresByParentId(@PathVariable Long parentId){
        return ResponseEntity.status(HttpStatus.OK).body(genre.getSubGenresByParentId(parentId));
    }

    @GetMapping("/usage/{genreId}")
    public ResponseEntity<?> isGenreInUse(@PathVariable Long genreId){
        return  ResponseEntity.status(HttpStatus.OK).body(genre.isGenreInUse(genreId));
    }

    @GetMapping("/count/{genreId}")
    public ResponseEntity<?> countBookByGenre(@PathVariable Long genreId){
        return ResponseEntity.status(HttpStatus.OK).body(genre.getBookCountByGenre(genreId));
    }

}
