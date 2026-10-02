package com.franquicias.api.franchise_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockRequest(@NotNull @PositiveOrZero Integer stock) {}