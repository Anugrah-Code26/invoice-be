package com.invoice.backend.service.client;

import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.common.exceptions.DataNotFoundException;

import java.util.List;

public interface ClientService {
    Client createClient(ClientDTO clientDTO, User user);
    List<Client> getAllClients(User user);
    Client getClientById(Long id, User user) throws DataNotFoundException;
    Client updateClient(Long id, ClientDTO clientDTO, User user) throws DataNotFoundException;
    void deleteClient(Long id, User user) throws DataNotFoundException;
}
