package com.invoice.backend.service.client;

import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import com.invoice.backend.infrastructure.client.dto.ClientResponseDTO;

import java.util.List;

public interface ClientService {
    Client createClient(ClientDTO clientDTO);
    List<ClientResponseDTO> searchClients(String name, String email, String phoneNumber);
    List<Client> getAllClients();
    Client getClientById(Long id) throws DataNotFoundException;
    Client updateClient(Long id, ClientDTO clientDTO) throws DataNotFoundException;
    void deleteClient(Long id) throws DataNotFoundException;
}
