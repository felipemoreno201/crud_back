package com.epico.crud.service.mapper;

import com.epico.crud.domain.Autor;
import com.epico.crud.domain.Libro;
import com.epico.crud.service.dto.AutorDTO;
import com.epico.crud.service.dto.LibroDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Libro} and its DTO {@link LibroDTO}.
 */
@Mapper(componentModel = "spring")
public interface LibroMapper extends EntityMapper<LibroDTO, Libro> {
    @Mapping(target = "autor", source = "autor", qualifiedByName = "autorId")
    LibroDTO toDto(Libro s);

    @Named("autorId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    AutorDTO toDtoAutorId(Autor autor);
}
