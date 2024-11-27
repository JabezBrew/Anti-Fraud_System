package antifraud.repo;

import antifraud.entity.StolenCards;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StolenCardsRepository extends JpaRepository<StolenCards, Long> {

    List<StolenCards> findByOrderByNumber();
    boolean existsByNumber(String number);
    @Transactional
    void deleteByNumber(String number);
}
