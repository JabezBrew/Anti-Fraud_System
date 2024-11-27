package antifraud.controllers;

import antifraud.dto.UserDTO;
import antifraud.services.UserDetailsServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AdminController {

    private final UserDetailsServiceImpl userDetailsService;

    public AdminController(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PutMapping("/role")
    public UserDTO changeRole(@RequestBody Map<String, String> roleChange) {
        return userDetailsService.changeRole(roleChange);
    }

    @PutMapping("/access")
    public Map<String, String> changeAccess(@RequestBody Map<String, String> accessChange) {
        userDetailsService.changeUserAccess(accessChange);
        return Map.of("status", "User "+accessChange.get("username")+" "+accessChange.get("operation").toLowerCase()+"ed!") ;
    }

    @DeleteMapping("/user/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        userDetailsService.deleteUser(username);
        return Map.of("username", username, "status", "Deleted successfully!");
    }

}
