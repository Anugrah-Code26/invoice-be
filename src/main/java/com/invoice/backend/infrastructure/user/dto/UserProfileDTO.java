package com.invoice.backend.infrastructure.user.dto;

import com.invoice.backend.entity.user.User;
import lombok.Data;

@Data
public class UserProfileDTO {
    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    public static UserProfileDTO fromEntity(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        return dto;
    }
}
