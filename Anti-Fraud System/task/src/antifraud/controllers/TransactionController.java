package antifraud.controllers;

import antifraud.entity.StolenCards;
import antifraud.entity.SuspiciousIPAddresses;
import antifraud.entity.Transaction;
import antifraud.services.TransactionServices;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    TransactionServices transactionServices;

    public TransactionController(TransactionServices transactionServices) {
        this.transactionServices = transactionServices;
    }

    @PostMapping( "/transaction")
    public Map<String, String> createTransaction(@RequestBody @Valid Transaction transaction) {
        return transactionServices.createTransaction(transaction);
    }

    @PutMapping("/transaction")
    public Transaction updateTransaction(@RequestBody Map<String, String> feedback) {
        return transactionServices.updateTransaction(feedback);
    }

    @GetMapping("/history")
    public List<Transaction> getHistory() {
        return transactionServices.getHistory();
    }

    @GetMapping("/history/{number}")
    public List<Transaction> getTransaction(@PathVariable String number) {
        return transactionServices.getTransactionsByCard(number);
    }

    @GetMapping("/suspicious-ip")
    public List<SuspiciousIPAddresses> getIpSuspicious() {
        return transactionServices.getSuspiciousIps();
    }

    @PostMapping("/suspicious-ip")
    public SuspiciousIPAddresses addIpSuspicious(@RequestBody SuspiciousIPAddresses ip) {
        transactionServices.addToSuspiciousIps(ip);
        return ip;
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public Map<String, String> removeIpSuspicious(@PathVariable String ip) {
        transactionServices.removeIpSuspicious(ip);
        return Map.of("status", "IP " + ip + " successfully removed!");
    }

    @GetMapping("/stolencard")
    public List<StolenCards> getStolenCard() {
        return transactionServices.getStolenCards();
    }

    @PostMapping("/stolencard")
    public StolenCards addStolenCard(@RequestBody StolenCards card) {
        transactionServices.addToStolenCards(card);
        return card;
    }

    @DeleteMapping("/stolencard/{number}")
    public Map<String, String> removeStolenCard(@PathVariable String number) {
        transactionServices.removeStolenCard(number);
        return Map.of("status", "Card " + number + " successfully removed!");
    }
}
