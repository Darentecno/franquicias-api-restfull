package com.franquicias.api.franchise_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequest(@NotBlank String nombre, @NotNull @PositiveOrZero Integer stock) {}