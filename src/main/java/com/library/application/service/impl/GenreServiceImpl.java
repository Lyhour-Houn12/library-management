package com.library.application.service.impl;

import com.library.application.entity.Genre;
import com.library.application.exception.GenreException;
import com.library.application.mapper.GenreMapper;
import com.library.application.payload.dto.GenreDTO;
import com.library.application.repository.GenreRepository;
import com.library.application.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        genreRepository.delete(existingGenre);
    }
    @Override
    public List<GenreDTO> getAllActiveGenresWithSubGenres() {
        List<Genre> topLevelGenres = genreRepository.findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(topLevelGenres);
    }
    @Override
    public List<GenreDTO> getTopLevelGenres() {
        List<Genre> topLevelGenres = genreRepository.findByParentGenreIsNullAndActiveTrueOrderByDisplayOrderAsc();
        return genreMapper.toDTOList(topLevelGenres);
    }
    @Override
    public long getTotalActiveGenres() {
        return genreRepository.countByActiveTrue();
    }

    @Override
    public List<GenreDTO> getAllActiveGenres() {

        return List.of();
    }

    @Override
    public GenreDTO getGenreByCode(String code) {

        return null;
    }



    @Override
    public List<GenreDTO> getSubGenresByParentId(Long parentGenreId) {
        return List.of();
    }

    @Override
    public Page<GenreDTO> searchGenres(String searchTerm, Pageable pageable) {
        return null;
    }



    @Override
    public long getBookCountByGenre(Long genreId) {
        return 0;
    }

    @Override
    public boolean isGenreInUse(Long genreId) {
        return false;
    }

}
