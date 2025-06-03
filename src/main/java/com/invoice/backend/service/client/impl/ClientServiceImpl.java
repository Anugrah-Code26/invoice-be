package com.invoice.backend.service.client.impl;

import com.invoice.backend.common.exceptions.UnauthorizedException;
import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.auth.Claims;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.infrastructure.client.dto.ClientResponseDTO;
import com.invoice.backend.infrastructure.client.repository.ClientRepository;
import com.invoice.backend.infrastructure.user.repository.UserRepository;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Collection;
import org.hibernate.sql.ast.tree.expression.Collation;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.invoice.backend.service.client.specification.ClientSpecification.*;

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
    public List<ClientResponseDTO> searchClients(String name, String email, String phoneNumber) {
        Long userId = Claims.getUserIdFromJwt();

        Specification<Client> spec = Specification.where(hasUserId(userId))
                .and(hasName(name))
                .and(hasEmail(email))
                .and(hasPhoneNumber(phoneNumber));

        List<Client> clients = clientRepository.findAll(spec);
        if (clients.isEmpty()) {
            return Collections.emptyList();
        }

        return clients.stream()
                .map(ClientResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Client> getAllClients() {
        String role = Claims.getRoleFromJwt();

        if (!"SUPER_ADMIN".equals(role)){
            throw new UnauthorizedException("Unauthorized!");
        }

        return clientRepository.findAll();
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

        Client checkClient = clientRepository.findByIdAndUserId(id, user.getId());
        if (checkClient == null) {
            throw new UnauthorizedException("Unauthorized!");
        }

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

        Client checkClient = clientRepository.findByIdAndUserId(id, user.getId());
        if (checkClient == null) {
            throw new UnauthorizedException("Unauthorized!");
        }

        Client client = getClientById(id);
        clientRepository.delete(client);
    }
}
