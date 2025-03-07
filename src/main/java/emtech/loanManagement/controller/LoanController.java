package emtech.loanManagement.controller;

import emtech.loanManagement.dto.LoanRequest;
import emtech.loanManagement.entity.Loan;
import emtech.loanManagement.entity.User;
import emtech.loanManagement.repository.LoanRepository;
import emtech.loanManagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createLoan(@RequestBody LoanRequest request) {
        logger.info("Creating loan for amount: {}", request.getAmount());

        // Get the authenticated user from the security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Loan loan = new Loan();
        loan.setAmount(request.getAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setStatus("PENDING");
        loan.setUser(user);

        loanRepository.save(loan);
        logger.info("Loan created successfully for user: {}", user.getUsername());

        return ResponseEntity.ok("Loan created successfully");
    }

    @GetMapping("/my-loans")
    public ResponseEntity<List<Loan>> getMyLoans() {
        logger.info("Fetching loans for authenticated user");

        // Get the authenticated user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Loan> loans = loanRepository.findByUser(user);
        logger.info("Found {} loans for user: {}", loans.size(), user.getUsername());

        return ResponseEntity.ok(loans);
    }
}