package com.epico.crud.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibroMapperTest {

    private LibroMapper libroMapper;

    @BeforeEach
    public void setUp() {
        libroMapper = new LibroMapperImpl();
    }
}
