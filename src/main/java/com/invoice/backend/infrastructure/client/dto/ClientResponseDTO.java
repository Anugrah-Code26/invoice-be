package com.invoice.backend.infrastructure.client.dto;

import com.invoice.backend.entity.client.Client;
import lombok.Data;

@Data
public class ClientResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

    public static ClientResponseDTO fromEntity(Client client) {
        ClientResponseDTO response = new ClientResponseDTO();
        response.setId(client.getId());
        response.setName(client.getName());
        response.setEmail(client.getEmail());
        response.setPhoneNumber(client.getPhoneNumber());
        return response;
    }
}
