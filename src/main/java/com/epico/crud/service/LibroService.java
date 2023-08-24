package com.epico.crud.service;

import com.epico.crud.service.dto.LibroDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.epico.crud.domain.Libro}.
 */
public interface LibroService {
    /**
     * Save a libro.
     *
     * @param libroDTO the entity to save.
     * @return the persisted entity.
     */
    LibroDTO save(LibroDTO libroDTO);

    /**
     * Updates a libro.
     *
     * @param libroDTO the entity to update.
     * @return the persisted entity.
     */
    LibroDTO update(LibroDTO libroDTO);

    /**
     * Partially updates a libro.
     *
     * @param libroDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<LibroDTO> partialUpdate(LibroDTO libroDTO);

    /**
     * Get all the libros.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<LibroDTO> findAll(Pageable pageable);

    /**
     * Get the "id" libro.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<LibroDTO> findOne(Long id);

    /**
     * Delete the "id" libro.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
