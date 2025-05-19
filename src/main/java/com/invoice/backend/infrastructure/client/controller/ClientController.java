package com.invoice.backend.infrastructure.client.controller;


import com.invoice.backend.entity.client.Client;
import com.invoice.backend.entity.user.User;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.service.user.UserService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Client> createClient(
            @Valid @RequestBody ClientDTO clientDTO,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(clientDTO, user));
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAllClients(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(clientService.getAllClients(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(clientService.getClientById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDTO clientDTO,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        return ResponseEntity.ok(clientService.updateClient(id, clientDTO, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) throws DataNotFoundException {
        clientService.deleteClient(id, user);
        return ResponseEntity.noContent().build();
    }
}