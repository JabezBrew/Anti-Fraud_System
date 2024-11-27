package antifraud.services;

import antifraud.entity.Role;
import antifraud.repo.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RolesLoader {

    private final RoleRepository roleRepository;

    @Autowired
    public RolesLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
        createRoles();
    }

    private void createRoles() {
        if (roleRepository.count() == 0) {
            try {
                roleRepository.save(new Role("ROLE_ADMINISTRATOR"));
                roleRepository.save(new Role("ROLE_MERCHANT"));
                roleRepository.save(new Role("ROLE_SUPPORT"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Roles already exist");
        }

    }
}
