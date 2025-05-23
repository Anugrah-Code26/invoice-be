package com.invoice.backend.service.client.impl;

import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.auth.Claims;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.infrastructure.client.repository.ClientRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Override
    public Client createClient(ClientDTO clientDTO) {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Client client = new Client();
        client.setUser(user);
        client.setName(clientDTO.getName());
        client.setAddress(clientDTO.getAddress());
        client.setEmail(clientDTO.getEmail());
        client.setPhoneNumber(clientDTO.getPhoneNumber());
        client.setPaymentPreferences(clientDTO.getPaymentPreferences());

        return clientRepository.save(client);
    }

    @Override
    public List<Client> getAllClients() {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return clientRepository.findByUserId(user.getId());
    }

    @Override
    public Client getClientById(Long id) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        return clientRepository.findById(id)
                .filter(client -> client.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new DataNotFoundException("Client not found"));
    }

    @Override
    public Client updateClient(Long id, ClientDTO clientDTO) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Client client = getClientById(id);
        client.setName(clientDTO.getName());
        client.setAddress(clientDTO.getAddress());
        client.setEmail(clientDTO.getEmail());
        client.setPhoneNumber(clientDTO.getPhoneNumber());
        client.setPaymentPreferences(clientDTO.getPaymentPreferences());

        return clientRepository.save(client);
    }

    @Override
    public void deleteClient(Long id) throws DataNotFoundException {
        Long userId = Claims.getUserIdFromJwt();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        Client client = getClientById(id);
        clientRepository.delete(client);
    }
}
