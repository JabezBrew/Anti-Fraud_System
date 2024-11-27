package antifraud.repo;

import antifraud.entity.SuspiciousIPAddresses;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIPAddressesRepository extends JpaRepository<SuspiciousIPAddresses, Long> {

    List<SuspiciousIPAddresses> findByOrderByIp();
    boolean existsByIp(String ip);

    @Transactional
    void deleteByIp(String ip);
}
