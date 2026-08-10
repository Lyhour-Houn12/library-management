package com.library.application.service.impl;

import com.library.application.entity.Genre;
import com.library.application.exception.GenreException;
import com.library.application.mapper.GenreMapper;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.payload.response.PageResponse;
import com.library.application.repository.GenreRepository;
import com.library.application.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Override
    public GenreDTO createGenre(GenreDTO genreDTO) {
        if (genreRepository.existsByCode(genreDTO.getCode())) {
            throw new GenreException("Genre with code " + genreDTO.getCode() + " already exists");
        }

        // validate parent genre if provided
        if (genreDTO.getParentGenreId() != null) {
            Genre parentGenre = genreRepository.findById(genreDTO.getParentGenreId())
                    .orElseThrow(() -> new GenreException("Parent genre with ID: " + genreDTO.getParentGenreId() + " not found"));

            if (!parentGenre.getActive()) {
                throw new GenreException("Can not set inactive genre as parent");
            }
        }
        Genre genre = genreMapper.toEntity(genreDTO);
        genre = genreRepository.save(genre);

        return genreMapper.toDTO(genre);
    }

    public List<GenreDTO> createGenresBulk(List<GenreDTO> genreDTOs) {
        if(genreDTOs == null || genreDTOs.isEmpty()) {
            throw new GenreException("Genres cannot be empty");
        }

        for(GenreDTO genreDTO : genreDTOs){
            // Check if genre duplicate code
            Long duplicateCode = genreDTOs.stream()
                    .filter(g -> g.getCode().equals(genreDTO.getCode()))
                    .count();
            if (duplicateCode > 1){
                throw new GenreException("Duplicate genre code " + genreDTO.getCode());
            }

            // check if code already exists
            if(genreRepository.existsByCode(genreDTO.getCode())){
                throw new GenreException("Genre with code " + genreDTO.getCode() + " already exists");
            }

            if(genreDTO.getParentGenreId() != null){
                Genre parentGenre = genreRepository.findById(genreDTO.getParentGenreId())
                        .orElseThrow(() -> new GenreException("Parent genre with ID: " + genreDTO.getParentGenreId() + " not found"));
                if (!parentGenre.getActive()) {
                    throw new GenreException("Can not set inactive genre as parent");
                }
            }
        }
        List<Genre> genreToSave = new ArrayList<>();
        for(GenreDTO genreDTO : genreDTOs){
            Genre genre = genreMapper.toEntity(genreDTO);
            genreToSave.add(genre);
        }
        List<Genre> savedGenres = genreRepository.saveAll(genreToSave);

        return savedGenres.stream()
                .map(genre -> genreMapper.toDTO(genre, false))
                .toList();
    }


    @Override
    public GenreDTO getGenreById(Long genreId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreException("Genre with id: " + genreId + " not found" ));
        return  genreMapper.toDTO(genre);
    }

    @Override
    public GenreDTO updateGenre(Long genreId, GenreDTO genreDTO) {
        Genre existingGenre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreException("Genre with id: " + genreId + " not found" ));
        // Check if code is being changed and if new code already exists
        if(!existingGenre.getCode().equals(genreDTO.getCode())){
            if(genreRepository.existsByCode(genreDTO.getCode())){
                throw new GenreException("Genre with code " + genreDTO.getCode() + " already exists");
            }
        }
        // validate parent genre if provided
        if(genreDTO.getParentGenreId() != null){
            // Can not set self as parent
            if(genreDTO.getParentGenreId().equals(genreId)){
                throw new GenreException("Genre can not be its own");
            }

            Genre parentGenre = genreRepository.findById(genreDTO.getParentGenreId())
                    .orElseThrow(() -> new GenreException("Parent genre with ID " + genreDTO.getParentGenreId() + " not found"));
            if (!parentGenre.getActive()) {
                throw new GenreException("Cannot set an inactive genre as parent");
            }
            if(isCircularReference(genreId, genreDTO.getParentGenreId())){
                throw new GenreException("Circular reference detected: parent genre cannot be a descendant of this genre");
            }

        }

        genreMapper.updateEntityFromDto(genreDTO, existingGenre);
        Genre updatedGenre =  genreRepository.save(existingGenre);
        return genreMapper.toDTO(updatedGenre) ;
    }
    @Override
    public void deleteGenre(Long genreId) {
        Genre existingGenre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreException("Genre with id: " + genreId + " not found" ));
        existingGenre.setActive(false);
        genreRepository.save(existingGenre);
    }

    @Override
    public void hardDeleteGenre(Long genreId) {
        Genre existingGenre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreException("Genre with id: " + genreId + " not found" ));
        // check if genre is in use
        if(genreRepository.isGenreInUse(genreId)){
            throw new GenreException("Can not delete genre: it is currently assigned to one or more books");
        }

        // Check if genre has sub-genres
        if(!existingGenre.getSubGenres().isEmpty()){
            throw new GenreException("Can not delete genre: it has sub-genres. Delete or reassign sub-genres first");
        }

        genreRepository.delete(existingGenre);
    }

    @Override
    public GenreDTO getGenreByCode(String code) {
        Genre genre = genreRepository.findByCode(code)
                .orElseThrow(() -> new GenreException("Genre with code: " + code + " not found"));
        return genreMapper.toDTO(genre);
    }

    // ==================== QUERY OPERATIONS ====================

    @Override
    public List<GenreDTO> getTopLevelGenres() {
        List<Genre> topLevelGenres = genreRepository.findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(topLevelGenres, true); // include sub-genres for hierarchical view
    }


    @Override
    public List<GenreDTO> getAllActiveGenres() {
        List<Genre> genres = genreRepository.findByActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(genres, false);
    }

    @Override
    public List<GenreDTO> getAllActiveGenresWithSubGenres() {
        // Only fetch top-level genres (genres with no parent)
        List<Genre> genres = genreRepository.findByActiveTrueOrderByDisplayOrderAsc();
        // Convert to DTOs with sub-genres included (recursive)
        return genreMapper.toDTOList(genres, true);
    }

    @Override
    public PageResponse<GenreDTO> searchGenres(String searchTerm, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Genre> genrePage = genreRepository.searchGenres(searchTerm, pageable);
        return PageResponse.from(genrePage.map(genreMapper::toDTO));
    }

    @Override
    public List<GenreDTO> getSubGenresByParentId(Long parentGenreId) {
        if(!genreRepository.existsById(parentGenreId)){
            throw new GenreException("Parent genre with ID: " + parentGenreId + " not found");
        }
        List<Genre> genre = genreRepository.findByParentGenreIdAndActiveTrueOrderByDisplayOrderAsc(parentGenreId);
        return genreMapper.toDTOList(genre, true);
    }

    // ==================== STATISTICS ====================

    @Override
    public boolean isGenreInUse(Long genreId) {
        if(!genreRepository.existsById(genreId)){
            throw new GenreException("Genre with id: " + genreId + " not found");
        }
        return genreRepository.isGenreInUse(genreId);
    }

    @Override
    public long getBookCountByGenre(Long genreId) {
        if(!genreRepository.existsById(genreId)){
            throw new GenreException("Genre with id: " + genreId + " not found");
        }
        return genreRepository.countBooksByGenre(genreId);
    }
    @Override
    public long getTotalActiveGenres() {
        return genreRepository.countByActiveTrue();
    }

    /**
     * Check if setting a parent would create a circular reference
     */
    private boolean isCircularReference(Long genreId, Long parentGenreId) {
        Genre parent = genreRepository.findById(parentGenreId).orElse(null);
        while (parent != null){
            if(parent.getId().equals(genreId)){
                return true;
            }
            parent = parent.getParentGenre();
        }
        return false;
    }




}
