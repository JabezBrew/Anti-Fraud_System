package antifraud.controllers;

import antifraud.dto.UserDTO;
import antifraud.entity.User;
import antifraud.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserDetailsServiceImpl userDetailsService;

    public UserController(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/user")
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public UserDTO signUp(@RequestBody @Valid User user) {
        userDetailsService.saveUser(user);
        return UserDTO.mapToDTO(user);
    }

    @GetMapping("/list")
    public List<UserDTO> userList() {
        return userDetailsService.listAllUsers();
    }

}
