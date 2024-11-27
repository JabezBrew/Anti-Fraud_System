package antifraud.repo;

import antifraud.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query(value = "SELECT COUNT(DISTINCT ip) FROM transaction WHERE number = ?1 AND ip != ?2 AND " +
            "date >= DATE_SUB(?3, INTERVAL 1 HOUR) and date <= ?3", nativeQuery = true)
    int findTransactionsByCardNumberAndIp(String number, String ip, String date);

    @Query(value = "SELECT COUNT(DISTINCT region) FROM transaction WHERE number = ?1 AND region != ?2 AND " +
            "date >= DATE_SUB(?3, INTERVAL 1 HOUR) and date <= ?3", nativeQuery = true)
    int findTransactionsByCardNumberAndRegion(String number, String ip, String date);
    boolean existsByNumber(String number);
    List<Transaction> getTransactionByNumber(String number);
}
