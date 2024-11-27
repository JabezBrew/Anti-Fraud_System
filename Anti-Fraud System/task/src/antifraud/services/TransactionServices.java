package antifraud.services;

import antifraud.entity.StolenCards;
import antifraud.entity.SuspiciousIPAddresses;
import antifraud.entity.Transaction;
import antifraud.entity.TransactionLimit;
import antifraud.errors.*;
import antifraud.repo.StolenCardsRepository;
import antifraud.repo.SuspiciousIPAddressesRepository;
import antifraud.repo.TransactionRepository;
import antifraud.security.LuhnAlgorithm;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TransactionServices {

    InetAddressValidator validator = InetAddressValidator.getInstance();
    SuspiciousIPAddressesRepository suspiciousIPAddressesRepo;
    StolenCardsRepository stolenCardsRepo;
    TransactionRepository transactionRepo;
    private long MAX_ALLOWED = TransactionLimit.maxAllowed;
    private long MAX_MANUAL = TransactionLimit.maxManual;

    public TransactionServices(SuspiciousIPAddressesRepository suspiciousIPAddressesRepo,
                               StolenCardsRepository stolenCardsRepo, TransactionRepository transactionRepo) {
        this.suspiciousIPAddressesRepo = suspiciousIPAddressesRepo;
        this.stolenCardsRepo = stolenCardsRepo;
        this.transactionRepo = transactionRepo;
    }

    public String arrayToString(List<String> array) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator<String> iterator = array.iterator();
        while (iterator.hasNext()) {
            String reason = iterator.next();
            stringBuilder.append(reason);
            if (iterator.hasNext()) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    public void checkFieldValidation(Transaction transaction) {
        List<String> regions = List.of("EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA");
        if (!LuhnAlgorithm.isValidCreditCardNumber(transaction.getNumber())) {
            throw new BadRequestExceptions("Invalid card number");
        } else if (!validator.isValid(transaction.getIp())) {
            throw new BadRequestExceptions("Invalid IP address");
        } else if (!regions.contains(transaction.getRegion())) {
            throw new BadRequestExceptions("Invalid region");
        } else if (transaction.getFeedback() == null) {
            transaction.setFeedback("");
        }
    }
    public Map<String, String> createTransaction(Transaction transaction) {
        checkFieldValidation(transaction);
        int ipCorrelation = transactionRepo.findTransactionsByCardNumberAndIp(transaction.getNumber(),
                transaction.getIp(), transaction.getDate().toString());
        int regionCorrelation = transactionRepo.findTransactionsByCardNumberAndRegion(transaction.getNumber(),
                transaction.getRegion(), transaction.getDate().toString());
        List<String> prohibitedReasons = new ArrayList<>();
        List<String> manualProcessingReasons = new ArrayList<>();

        if (ipCorrelation > 2) {
            prohibitedReasons.add("ip-correlation");
        } else if (ipCorrelation == 2) {
            manualProcessingReasons.add("ip-correlation");
        }
        if (regionCorrelation > 2) {
            prohibitedReasons.add("region-correlation");
        } else if (regionCorrelation == 2) {
            manualProcessingReasons.add("region-correlation");
        }

        if (suspiciousIPAddressesRepo.existsByIp(transaction.getIp())) {
            prohibitedReasons.add("ip");
        }
        if (stolenCardsRepo.existsByNumber(transaction.getNumber())) {
            prohibitedReasons.add("card-number");
        }
        if (transaction.getAmount() > MAX_MANUAL) {
            prohibitedReasons.add("amount");
        } else if (transaction.getAmount() > MAX_ALLOWED && transaction.getAmount() <= MAX_MANUAL) {
            manualProcessingReasons.add("amount");
        }

        prohibitedReasons.sort(String::compareTo);
        manualProcessingReasons.sort(String::compareTo);


        if (transaction.getAmount() <= MAX_ALLOWED && ipCorrelation < 2 && regionCorrelation < 2) {
            transaction.setResult("ALLOWED");
            transactionRepo.save(transaction);
            return Map.of("result", "ALLOWED", "info", "none");
        } else {
            if (prohibitedReasons.size() > 0) {
                transaction.setResult("PROHIBITED");
                transactionRepo.save(transaction);
                return Map.of("result", "PROHIBITED", "info", arrayToString(prohibitedReasons));

            } else {
                transaction.setResult("MANUAL_PROCESSING");
                transactionRepo.save(transaction);
                return Map.of("result", "MANUAL_PROCESSING", "info", arrayToString(manualProcessingReasons));
            }
        }

    }

    public void feedbackAlgo(Transaction transaction) {
        double newAllowIncLimit = Math.ceil(0.8 * MAX_ALLOWED + 0.2 * transaction.getAmount());
        double newAllowDecLimit = Math.ceil(0.8 * MAX_ALLOWED - 0.2 * transaction.getAmount());
        double newManualIncLimit = Math.ceil(0.8 * MAX_MANUAL + 0.2 * transaction.getAmount());
        double newManualDecLimit = Math.ceil(0.8 * MAX_MANUAL - 0.2 * transaction.getAmount());

        switch (transaction.getResult()) {
            case "ALLOWED" -> {
                switch (transaction.getFeedback()) {
                    case "ALLOWED" -> throw new UnprocessableContentException("Feedback Exception");
                    case "MANUAL_PROCESSING" -> MAX_ALLOWED = (long) newAllowDecLimit;
                    case "PROHIBITED" -> {
                        MAX_ALLOWED = (long) newAllowDecLimit;
                        MAX_MANUAL = (long) newManualDecLimit;
                    }
                }
            }
            case "MANUAL_PROCESSING" -> {
                switch (transaction.getFeedback()) {
                    case "ALLOWED" -> MAX_ALLOWED = (long) newAllowIncLimit;
                    case "MANUAL_PROCESSING" -> throw new UnprocessableContentException("Feedback Exception");
                    case "PROHIBITED" -> MAX_MANUAL = (long) newManualDecLimit;
                }
            }
            case "PROHIBITED" -> {
                switch (transaction.getFeedback()) {
                    case "ALLOWED" -> {
                        MAX_ALLOWED = (long) newAllowIncLimit;
                        MAX_MANUAL = (long) newManualIncLimit;
                    }
                    case "MANUAL_PROCESSING" -> MAX_MANUAL = (long) newManualIncLimit;
                    case "PROHIBITED" -> throw new UnprocessableContentException("Feedback Exception");
                }
            }
        }
        TransactionLimit.maxManual = MAX_MANUAL;
        TransactionLimit.maxAllowed = MAX_ALLOWED;
    }

    public List<Transaction> getHistory() {
        return transactionRepo.findAll();
    }

    public List<Transaction> getTransactionsByCard(String number) {
        if (!LuhnAlgorithm.isValidCreditCardNumber(number)) {
            throw new BadRequestExceptions("Invalid card number");
        } else if (!transactionRepo.existsByNumber(number)) {
            throw new NotFoundException("Transactions not found");
        }
        return transactionRepo.getTransactionByNumber(number);
    }

    public Transaction updateTransaction(Map<String, String> feedback) {
        List<String> feedbacks = List.of("ALLOWED", "MANUAL_PROCESSING", "PROHIBITED");
        long transactionId = Long.parseLong(feedback.get("transactionId"));
        String feedbackText = feedback.get("feedback");
        Transaction transaction = transactionRepo.findById(transactionId).orElseThrow(() -> new NotFoundException("Transaction not found"));
        if (!feedbacks.contains(feedbackText)) {
            throw new BadRequestExceptions("Invalid feedback");
        } else if (!transaction.getFeedback().isEmpty()) {
            throw new UserConflictException("Feedback already exists");
        }

        transaction.setFeedback(feedbackText);
        feedbackAlgo(transaction);
        transactionRepo.save(transaction);
        return transaction;
    }

    public List<SuspiciousIPAddresses> getSuspiciousIps() {
        return suspiciousIPAddressesRepo.findByOrderByIp();
    }

    public void addToSuspiciousIps(SuspiciousIPAddresses ip) {
        if (suspiciousIPAddressesRepo.existsByIp(ip.getIp())) {
            throw new IpAddressOrCardConflictException("IP address already exists");
        } else {
            if (validator.isValid(ip.getIp())) {
                suspiciousIPAddressesRepo.save(ip);
            } else {
                throw new BadRequestExceptions("IP address is not valid");
            }
        }
    }

    public void removeIpSuspicious(String ip) {
        if (validator.isValid(ip)) {
            if (!suspiciousIPAddressesRepo.existsByIp(ip)) {
                throw new NotFoundException("IP address does not exist");
            } else {
                suspiciousIPAddressesRepo.deleteByIp(ip);
            }
        } else {
            throw new BadRequestExceptions("IP address is not valid");
        }
    }

    public List<StolenCards> getStolenCards() {
        return stolenCardsRepo.findByOrderByNumber();
    }

    public void addToStolenCards(StolenCards card) {
        if (stolenCardsRepo.existsByNumber(card.getNumber())) {
            throw new IpAddressOrCardConflictException("Card number already exists");
        } else {
            if (LuhnAlgorithm.isValidCreditCardNumber(card.getNumber())) {
                stolenCardsRepo.save(card);
            } else {
                throw new BadRequestExceptions("Card number is not valid");
            }
        }
    }

    public void removeStolenCard(String cardNumber) {
        if (LuhnAlgorithm.isValidCreditCardNumber(cardNumber)) {
            if (!stolenCardsRepo.existsByNumber(cardNumber)) {
                throw new NotFoundException("Card number does not exist");
            }
            stolenCardsRepo.deleteByNumber(cardNumber);
        } else {
            throw new BadRequestExceptions("Card number is not valid");
        }
    }
}
