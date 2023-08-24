package com.epico.crud.service.impl;

import com.epico.crud.domain.Libro;
import com.epico.crud.repository.LibroRepository;
import com.epico.crud.service.LibroService;
import com.epico.crud.service.dto.LibroDTO;
import com.epico.crud.service.mapper.LibroMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Libro}.
 */
@Service
@Transactional
public class LibroServiceImpl implements LibroService {

    private final Logger log = LoggerFactory.getLogger(LibroServiceImpl.class);

    private final LibroRepository libroRepository;

    private final LibroMapper libroMapper;

    public LibroServiceImpl(LibroRepository libroRepository, LibroMapper libroMapper) {
        this.libroRepository = libroRepository;
        this.libroMapper = libroMapper;
    }

    @Override
    public LibroDTO save(LibroDTO libroDTO) {
        log.debug("Request to save Libro : {}", libroDTO);
        Libro libro = libroMapper.toEntity(libroDTO);
        libro = libroRepository.save(libro);
        return libroMapper.toDto(libro);
    }

    @Override
    public LibroDTO update(LibroDTO libroDTO) {
        log.debug("Request to update Libro : {}", libroDTO);
        Libro libro = libroMapper.toEntity(libroDTO);
        libro = libroRepository.save(libro);
        return libroMapper.toDto(libro);
    }

    @Override
    public Optional<LibroDTO> partialUpdate(LibroDTO libroDTO) {
        log.debug("Request to partially update Libro : {}", libroDTO);

        return libroRepository
            .findById(libroDTO.getId())
            .map(existingLibro -> {
                libroMapper.partialUpdate(existingLibro, libroDTO);

                return existingLibro;
            })
            .map(libroRepository::save)
            .map(libroMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LibroDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Libros");
        return libroRepository.findAll(pageable).map(libroMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LibroDTO> findOne(Long id) {
        log.debug("Request to get Libro : {}", id);
        return libroRepository.findById(id).map(libroMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Libro : {}", id);
        libroRepository.deleteById(id);
    }
}
