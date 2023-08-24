package com.epico.crud.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.epico.crud.domain.Autor} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AutorDTO implements Serializable {

    private Long id;

    private String nombre;

    private String correo;

    private String pais;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AutorDTO)) {
            return false;
        }

        AutorDTO autorDTO = (AutorDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, autorDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AutorDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", correo='" + getCorreo() + "'" +
            ", pais='" + getPais() + "'" +
            "}";
    }
}
