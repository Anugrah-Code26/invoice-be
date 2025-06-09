package com.invoice.backend.infrastructure.client.controller;


import com.invoice.backend.common.responses.ApiResponse;
import com.invoice.backend.infrastructure.client.dto.ClientDTO;
import com.invoice.backend.service.client.ClientService;
import com.invoice.backend.common.exceptions.DataNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        return ApiResponse.success(HttpStatus.OK.value(), "Create client success!", clientService.createClient(clientDTO));
    }

    @GetMapping
    public ResponseEntity<?> searchClients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber
    ) {
        return ApiResponse.success(HttpStatus.OK.value(), "Get client success!", clientService.searchClients(name, email, phoneNumber));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllClients() {
        return ApiResponse.success(HttpStatus.OK.value(), "Get all clients data success!", clientService.getAllClients());
    }

    @GetMapping("/user/all")
    public ResponseEntity<?> getClientsByUserId() {
        return ApiResponse.success(HttpStatus.OK.value(), "Get all user clients data success!", clientService.getClientsByUserId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) throws DataNotFoundException {
        return ApiResponse.success(HttpStatus.OK.value(), "Get client by id success!", clientService.getClientById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDTO clientDTO) throws DataNotFoundException {
        return ApiResponse.success(HttpStatus.OK.value(), "Update client by id success!", clientService.updateClient(id, clientDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id) throws DataNotFoundException {
        clientService.deleteClient(id);
        return ApiResponse.success("Client successfully deleted!");
    }
}