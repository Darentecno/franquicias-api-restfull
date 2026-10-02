package com.franquicias.api.franchise_service.dto;

import jakarta.validation.constraints.NotBlank;

public record NombreRequest(@NotBlank String nombre) {}