package com.epico.crud.web.rest;

import static com.epico.crud.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.epico.crud.IntegrationTest;
import com.epico.crud.domain.Libro;
import com.epico.crud.repository.LibroRepository;
import com.epico.crud.service.dto.LibroDTO;
import com.epico.crud.service.mapper.LibroMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link LibroResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class LibroResourceIT {

    private static final String DEFAULT_TITULO = "AAAAAAAAAA";
    private static final String UPDATED_TITULO = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final BigDecimal DEFAULT_PRECIO = new BigDecimal(1);
    private static final BigDecimal UPDATED_PRECIO = new BigDecimal(2);

    private static final String ENTITY_API_URL = "/api/libros";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private LibroMapper libroMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restLibroMockMvc;

    private Libro libro;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Libro createEntity(EntityManager em) {
        Libro libro = new Libro().titulo(DEFAULT_TITULO).descripcion(DEFAULT_DESCRIPCION).precio(DEFAULT_PRECIO);
        return libro;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Libro createUpdatedEntity(EntityManager em) {
        Libro libro = new Libro().titulo(UPDATED_TITULO).descripcion(UPDATED_DESCRIPCION).precio(UPDATED_PRECIO);
        return libro;
    }

    @BeforeEach
    public void initTest() {
        libro = createEntity(em);
    }

    @Test
    @Transactional
    void createLibro() throws Exception {
        int databaseSizeBeforeCreate = libroRepository.findAll().size();
        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);
        restLibroMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(libroDTO)))
            .andExpect(status().isCreated());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeCreate + 1);
        Libro testLibro = libroList.get(libroList.size() - 1);
        assertThat(testLibro.getTitulo()).isEqualTo(DEFAULT_TITULO);
        assertThat(testLibro.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testLibro.getPrecio()).isEqualByComparingTo(DEFAULT_PRECIO);
    }

    @Test
    @Transactional
    void createLibroWithExistingId() throws Exception {
        // Create the Libro with an existing ID
        libro.setId(1L);
        LibroDTO libroDTO = libroMapper.toDto(libro);

        int databaseSizeBeforeCreate = libroRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restLibroMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(libroDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllLibros() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        // Get all the libroList
        restLibroMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(libro.getId().intValue())))
            .andExpect(jsonPath("$.[*].titulo").value(hasItem(DEFAULT_TITULO)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].precio").value(hasItem(sameNumber(DEFAULT_PRECIO))));
    }

    @Test
    @Transactional
    void getLibro() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        // Get the libro
        restLibroMockMvc
            .perform(get(ENTITY_API_URL_ID, libro.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(libro.getId().intValue()))
            .andExpect(jsonPath("$.titulo").value(DEFAULT_TITULO))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.precio").value(sameNumber(DEFAULT_PRECIO)));
    }

    @Test
    @Transactional
    void getNonExistingLibro() throws Exception {
        // Get the libro
        restLibroMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingLibro() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        int databaseSizeBeforeUpdate = libroRepository.findAll().size();

        // Update the libro
        Libro updatedLibro = libroRepository.findById(libro.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedLibro are not directly saved in db
        em.detach(updatedLibro);
        updatedLibro.titulo(UPDATED_TITULO).descripcion(UPDATED_DESCRIPCION).precio(UPDATED_PRECIO);
        LibroDTO libroDTO = libroMapper.toDto(updatedLibro);

        restLibroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, libroDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(libroDTO))
            )
            .andExpect(status().isOk());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
        Libro testLibro = libroList.get(libroList.size() - 1);
        assertThat(testLibro.getTitulo()).isEqualTo(UPDATED_TITULO);
        assertThat(testLibro.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testLibro.getPrecio()).isEqualByComparingTo(UPDATED_PRECIO);
    }

    @Test
    @Transactional
    void putNonExistingLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, libroDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(libroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(libroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(libroDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateLibroWithPatch() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        int databaseSizeBeforeUpdate = libroRepository.findAll().size();

        // Update the libro using partial update
        Libro partialUpdatedLibro = new Libro();
        partialUpdatedLibro.setId(libro.getId());

        partialUpdatedLibro.titulo(UPDATED_TITULO);

        restLibroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLibro.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedLibro))
            )
            .andExpect(status().isOk());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
        Libro testLibro = libroList.get(libroList.size() - 1);
        assertThat(testLibro.getTitulo()).isEqualTo(UPDATED_TITULO);
        assertThat(testLibro.getDescripcion()).isEqualTo(DEFAULT_DESCRIPCION);
        assertThat(testLibro.getPrecio()).isEqualByComparingTo(DEFAULT_PRECIO);
    }

    @Test
    @Transactional
    void fullUpdateLibroWithPatch() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        int databaseSizeBeforeUpdate = libroRepository.findAll().size();

        // Update the libro using partial update
        Libro partialUpdatedLibro = new Libro();
        partialUpdatedLibro.setId(libro.getId());

        partialUpdatedLibro.titulo(UPDATED_TITULO).descripcion(UPDATED_DESCRIPCION).precio(UPDATED_PRECIO);

        restLibroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedLibro.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedLibro))
            )
            .andExpect(status().isOk());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
        Libro testLibro = libroList.get(libroList.size() - 1);
        assertThat(testLibro.getTitulo()).isEqualTo(UPDATED_TITULO);
        assertThat(testLibro.getDescripcion()).isEqualTo(UPDATED_DESCRIPCION);
        assertThat(testLibro.getPrecio()).isEqualByComparingTo(UPDATED_PRECIO);
    }

    @Test
    @Transactional
    void patchNonExistingLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, libroDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(libroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(libroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamLibro() throws Exception {
        int databaseSizeBeforeUpdate = libroRepository.findAll().size();
        libro.setId(count.incrementAndGet());

        // Create the Libro
        LibroDTO libroDTO = libroMapper.toDto(libro);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restLibroMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(libroDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Libro in the database
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteLibro() throws Exception {
        // Initialize the database
        libroRepository.saveAndFlush(libro);

        int databaseSizeBeforeDelete = libroRepository.findAll().size();

        // Delete the libro
        restLibroMockMvc
            .perform(delete(ENTITY_API_URL_ID, libro.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Libro> libroList = libroRepository.findAll();
        assertThat(libroList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
