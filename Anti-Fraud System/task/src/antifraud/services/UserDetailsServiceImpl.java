package antifraud.services;

import antifraud.dto.UserDTO;
import antifraud.entity.Role;
import antifraud.entity.User;
import antifraud.errors.BadRequestExceptions;
import antifraud.errors.NotFoundException;
import antifraud.errors.UserConflictException;
import antifraud.repo.RoleRepository;
import antifraud.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    public UserDetailsServiceImpl(UserRepository userRepo, RoleRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUsername(username);
        return user.orElse(null);
    }

    //User
    public Role defineRole(String role) {
        return roleRepo.findByRole(role).orElseThrow(() -> new NotFoundException("Role not found"));
    }
    public void saveUser(User user) {
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new UserConflictException("User already exists");
        } else {
            if (userRepo.count() == 0) {
                user.setRoles(Set.of(defineRole("ROLE_ADMINISTRATOR")));
            } else {
                user.setRoles(Set.of(defineRole("ROLE_MERCHANT")));
            }
        }
        user.setPassword(getEncoder().encode(user.getPassword()));
        user.setUsername(user.getUsername().toLowerCase());
        userRepo.save(user);
    }

    public List<UserDTO> listAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        for (User user : userRepo.findByOrderById()) {
            users.add(UserDTO.mapToDTO(user));
        }
        return users;
    }

    public void deleteUser(String username) {
        if (!userRepo.existsByUsername(username)) {
            throw new NotFoundException("User not found!");
        }
        userRepo.deleteByUsername(username);
    }

    public UserDTO changeRole(Map<String, String> roleChange) {

        User user = userRepo.findByUsername(roleChange.get("username")).orElseThrow(() -> new NotFoundException("User not found!"));
        Role role = roleRepo.findByRole("ROLE_"+roleChange.get("role")).orElseThrow(() -> new BadRequestExceptions("Role not found!"));
        if (Objects.equals("ROLE_"+roleChange.get("role"), "ROLE_ADMINISTRATOR")) {
            throw new BadRequestExceptions("Cannot change role to ADMINISTRATOR!");
        } else {
            //if another user has support role, throw 409
            if (Objects.equals("ROLE_"+roleChange.get("role"), "ROLE_SUPPORT") && userRepo.findByRoles(role).isPresent()) {
                throw new UserConflictException("Another user has SUPPORT role!");
            }
            user.getRoles().clear();
            user.getRoles().add(role);
            userRepo.save(user);
        }
        return UserDTO.mapToDTO(user);
    }

    public void changeUserAccess(Map<String, String> accessChange) {
        User user = userRepo.findByUsername(accessChange.get("username")).orElseThrow(() -> new NotFoundException("User not found!"));
        if (user.getRoles().contains(defineRole("ROLE_ADMINISTRATOR"))) {
            throw new BadRequestExceptions("Cannot lock ADMINISTRATOR!");
        }
        if (accessChange.get("operation").equalsIgnoreCase("lock")) {
            lock(user);

        } else if (accessChange.get("operation").equalsIgnoreCase("unlock")) {
            unlock(user);
        } else {
            throw new NotFoundException("Operation not found!");
        }

    }

    public void lock(User user) {
        if (user.getRoles().stream().anyMatch(r -> Objects.equals("ROLE_"+r.getRole(), "ROLE_ADMINISTRATOR"))) {
            throw new BadRequestExceptions("Can't lock the ADMINISTRATOR!");
        }

        user.setAccountNonLocked(false);
        //user.setLockTime(new Date());
        userRepo.save(user);
    }

    public void unlock(User user) {
        user.setAccountNonLocked(true);
        userRepo.save(user);
    }

    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
