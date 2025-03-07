package emtech.loanManagement.controller;

import emtech.loanManagement.entity.Loan;
import emtech.loanManagement.entity.Repayment;
import emtech.loanManagement.entity.User;
import emtech.loanManagement.repository.LoanRepository;
import emtech.loanManagement.repository.RepaymentRepository;
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
@RequestMapping("/api/repayments")
public class RepaymentController {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentController.class);

    @Autowired
    private RepaymentRepository repaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/make-payment/{loanId}")
    public ResponseEntity<?> makePayment(@PathVariable Long loanId, @RequestParam Double amount) {
        logger.info("Processing repayment of {} for loan ID: {}", amount, loanId);

        // Get the authenticated user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Ensure the loan belongs to the authenticated user
        if (!loan.getUser().getId().equals(user.getId())) {
            logger.warn("User {} attempted to make a payment for a loan that doesn't belong to them: {}", user.getUsername(), loanId);
            return ResponseEntity.status(403).body("You can only make payments for your own loans");
        }

        // Validate the payment amount
        if (amount <= 0) {
            logger.warn("Invalid payment amount: {}", amount);
            return ResponseEntity.badRequest().body("Payment amount must be greater than 0");
        }

        // Check if the payment exceeds the remaining balance
        if (amount > loan.getRemainingBalance()) {
            logger.warn("Payment amount {} exceeds remaining balance {} for loan ID: {}", amount, loan.getRemainingBalance(), loanId);
            return ResponseEntity.badRequest().body("Payment amount cannot exceed the remaining balance of " + loan.getRemainingBalance());
        }

        // Create the repayment record
        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setAmount(amount);
        repaymentRepository.save(repayment);

        // Update the loan's remaining balance
        loan.setRemainingBalance(loan.getRemainingBalance() - amount);
        if (loan.getRemainingBalance() == 0) {
            loan.setStatus("PAID");
            logger.info("Loan ID: {} fully paid off by user: {}", loanId, user.getUsername());
        }
        loanRepository.save(loan);

        logger.info("Repayment of {} recorded for loan ID: {} by user: {}", amount, loanId, user.getUsername());
        return ResponseEntity.ok("Payment of " + amount + " recorded successfully. Remaining balance: " + loan.getRemainingBalance());
    }

    @GetMapping("/my-repayments")
    public ResponseEntity<List<Repayment>> getMyRepayments() {
        logger.info("Fetching repayment history for authenticated user");

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Repayment> repayments = repaymentRepository.findByLoanUser(user);
        logger.info("Found {} repayments for user: {}", repayments.size(), user.getUsername());

        return ResponseEntity.ok(repayments);
    }
}