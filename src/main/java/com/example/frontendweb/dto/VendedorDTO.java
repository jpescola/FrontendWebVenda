package com.example.frontendweb.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record VendedorDTO(
        Long id,
        String nome,
        String foto
) {
    public VendedorDTO(){this(null, null, null);}
}

