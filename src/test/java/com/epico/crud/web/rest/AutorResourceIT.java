package com.epico.crud.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.epico.crud.IntegrationTest;
import com.epico.crud.domain.Autor;
import com.epico.crud.repository.AutorRepository;
import com.epico.crud.service.dto.AutorDTO;
import com.epico.crud.service.mapper.AutorMapper;
import jakarta.persistence.EntityManager;
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
 * Integration tests for the {@link AutorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AutorResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_CORREO = "AAAAAAAAAA";
    private static final String UPDATED_CORREO = "BBBBBBBBBB";

    private static final String DEFAULT_PAIS = "AAAAAAAAAA";
    private static final String UPDATED_PAIS = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/autors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private AutorRepository autorRepository;

    @Autowired
    private AutorMapper autorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAutorMockMvc;

    private Autor autor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Autor createEntity(EntityManager em) {
        Autor autor = new Autor().nombre(DEFAULT_NOMBRE).correo(DEFAULT_CORREO).pais(DEFAULT_PAIS);
        return autor;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Autor createUpdatedEntity(EntityManager em) {
        Autor autor = new Autor().nombre(UPDATED_NOMBRE).correo(UPDATED_CORREO).pais(UPDATED_PAIS);
        return autor;
    }

    @BeforeEach
    public void initTest() {
        autor = createEntity(em);
    }

    @Test
    @Transactional
    void createAutor() throws Exception {
        int databaseSizeBeforeCreate = autorRepository.findAll().size();
        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);
        restAutorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(autorDTO)))
            .andExpect(status().isCreated());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeCreate + 1);
        Autor testAutor = autorList.get(autorList.size() - 1);
        assertThat(testAutor.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testAutor.getCorreo()).isEqualTo(DEFAULT_CORREO);
        assertThat(testAutor.getPais()).isEqualTo(DEFAULT_PAIS);
    }

    @Test
    @Transactional
    void createAutorWithExistingId() throws Exception {
        // Create the Autor with an existing ID
        autor.setId(1L);
        AutorDTO autorDTO = autorMapper.toDto(autor);

        int databaseSizeBeforeCreate = autorRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restAutorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(autorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllAutors() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        // Get all the autorList
        restAutorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(autor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].correo").value(hasItem(DEFAULT_CORREO)))
            .andExpect(jsonPath("$.[*].pais").value(hasItem(DEFAULT_PAIS)));
    }

    @Test
    @Transactional
    void getAutor() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        // Get the autor
        restAutorMockMvc
            .perform(get(ENTITY_API_URL_ID, autor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(autor.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.correo").value(DEFAULT_CORREO))
            .andExpect(jsonPath("$.pais").value(DEFAULT_PAIS));
    }

    @Test
    @Transactional
    void getNonExistingAutor() throws Exception {
        // Get the autor
        restAutorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingAutor() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        int databaseSizeBeforeUpdate = autorRepository.findAll().size();

        // Update the autor
        Autor updatedAutor = autorRepository.findById(autor.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedAutor are not directly saved in db
        em.detach(updatedAutor);
        updatedAutor.nombre(UPDATED_NOMBRE).correo(UPDATED_CORREO).pais(UPDATED_PAIS);
        AutorDTO autorDTO = autorMapper.toDto(updatedAutor);

        restAutorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, autorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(autorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
        Autor testAutor = autorList.get(autorList.size() - 1);
        assertThat(testAutor.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testAutor.getCorreo()).isEqualTo(UPDATED_CORREO);
        assertThat(testAutor.getPais()).isEqualTo(UPDATED_PAIS);
    }

    @Test
    @Transactional
    void putNonExistingAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, autorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(autorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(autorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(autorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateAutorWithPatch() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        int databaseSizeBeforeUpdate = autorRepository.findAll().size();

        // Update the autor using partial update
        Autor partialUpdatedAutor = new Autor();
        partialUpdatedAutor.setId(autor.getId());

        partialUpdatedAutor.pais(UPDATED_PAIS);

        restAutorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAutor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAutor))
            )
            .andExpect(status().isOk());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
        Autor testAutor = autorList.get(autorList.size() - 1);
        assertThat(testAutor.getNombre()).isEqualTo(DEFAULT_NOMBRE);
        assertThat(testAutor.getCorreo()).isEqualTo(DEFAULT_CORREO);
        assertThat(testAutor.getPais()).isEqualTo(UPDATED_PAIS);
    }

    @Test
    @Transactional
    void fullUpdateAutorWithPatch() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        int databaseSizeBeforeUpdate = autorRepository.findAll().size();

        // Update the autor using partial update
        Autor partialUpdatedAutor = new Autor();
        partialUpdatedAutor.setId(autor.getId());

        partialUpdatedAutor.nombre(UPDATED_NOMBRE).correo(UPDATED_CORREO).pais(UPDATED_PAIS);

        restAutorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedAutor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedAutor))
            )
            .andExpect(status().isOk());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
        Autor testAutor = autorList.get(autorList.size() - 1);
        assertThat(testAutor.getNombre()).isEqualTo(UPDATED_NOMBRE);
        assertThat(testAutor.getCorreo()).isEqualTo(UPDATED_CORREO);
        assertThat(testAutor.getPais()).isEqualTo(UPDATED_PAIS);
    }

    @Test
    @Transactional
    void patchNonExistingAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, autorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(autorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(autorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamAutor() throws Exception {
        int databaseSizeBeforeUpdate = autorRepository.findAll().size();
        autor.setId(count.incrementAndGet());

        // Create the Autor
        AutorDTO autorDTO = autorMapper.toDto(autor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAutorMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(autorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Autor in the database
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteAutor() throws Exception {
        // Initialize the database
        autorRepository.saveAndFlush(autor);

        int databaseSizeBeforeDelete = autorRepository.findAll().size();

        // Delete the autor
        restAutorMockMvc
            .perform(delete(ENTITY_API_URL_ID, autor.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Autor> autorList = autorRepository.findAll();
        assertThat(autorList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
