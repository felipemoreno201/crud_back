package com.epico.crud.service.mapper;

import com.epico.crud.domain.Autor;
import com.epico.crud.service.dto.AutorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Autor} and its DTO {@link AutorDTO}.
 */
@Mapper(componentModel = "spring")
public interface AutorMapper extends EntityMapper<AutorDTO, Autor> {}
