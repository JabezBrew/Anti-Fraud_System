package antifraud.dto;

import antifraud.entity.Role;
import antifraud.entity.User;

import java.util.List;
import java.util.Set;

public record UserDTO(Long id, String name, String username, String role) {

    public static UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getUsername(), getStringFromSetOfRoles(user.getRoles()));
    }

    public static String getStringFromSetOfRoles(Set<Role> roles) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Role role : roles) {
            stringBuilder.append(role.getRole());
        }
        return stringBuilder.toString().split("ROLE_")[1];
    }
}
