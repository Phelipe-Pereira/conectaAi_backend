package com.conectaai.dto.customer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerSummaryDto(

        Long id,

        @JsonProperty("external_id")
        String externalId,

        @JsonProperty("full_name")
        String fullName,

        String email
) {
}

