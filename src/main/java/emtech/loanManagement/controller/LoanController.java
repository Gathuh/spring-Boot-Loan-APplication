package emtech.loanManagement.controller;

import emtech.loanManagement.dto.LoanDTO;
import emtech.loanManagement.dto.RepaymentDTO;
import emtech.loanManagement.dto.UserDTO;
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
import java.util.stream.Collectors;

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

        // Validate request
        if (request.getAmount() == null || request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(new ErrorResponse("error", "Amount must be greater than 0"));
        }
        if (request.getInterestRate() == null || request.getInterestRate() < 0 || request.getInterestRate() > 100) {
            return ResponseEntity.badRequest().body(new ErrorResponse("error", "Interest rate must be between 0 and 100"));
        }

        // Get the authenticated user from the security context
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create and save the loan using the parameterized constructor
        Loan loan = new Loan(request.getAmount(), request.getInterestRate(), user);
        Loan savedLoan = loanRepository.save(loan);
        logger.info("Loan created successfully for user: {}", user.getUsername());

        // Convert to DTO and return structured response
        LoanDTO loanDTO = convertToDTO(savedLoan);
        return ResponseEntity.ok(new LoanResponse("success", "Loan created successfully", loanDTO));
    }

    @GetMapping("/my-loans")
    public ResponseEntity<?> getMyLoans() {
        logger.info("Fetching loans for authenticated user");

        // Get the authenticated user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch loans with repayments
        List<Loan> loans = loanRepository.findByUser(user);
        List<LoanDTO> loanDTOs = loans.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        logger.info("Found {} loans for user: {}", loans.size(), user.getUsername());

        return ResponseEntity.ok(loanDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLoan(@PathVariable Long id, @RequestBody LoanRequest request) {
        logger.info("Updating loan with id: {}", id);

        // Validate request
        if (request.getAmount() != null && request.getAmount() <= 0) {
            return ResponseEntity.badRequest().body(new ErrorResponse("error", "Amount must be greater than 0"));
        }
        if (request.getInterestRate() != null && (request.getInterestRate() < 0 || request.getInterestRate() > 100)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("error", "Interest rate must be between 0 and 100"));
        }

        // Get the authenticated user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        logger.debug("Authenticated user: {}", user.getUsername());

        // Fetch the loan
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        logger.debug("Loan user: {}", loan.getUser() != null ? loan.getUser().getUsername() : "null");

        // Authorization check with debugging
        if (loan.getUser() == null || !loan.getUser().getUsername().equals(user.getUsername())) {
            logger.warn("Unauthorized access attempt: Loan user ({}) does not match authenticated user ({})",
                    loan.getUser() != null ? loan.getUser().getUsername() : "null", user.getUsername());
            return ResponseEntity.status(403).body(new ErrorResponse("error", "Unauthorized to update this loan"));
        }

        // Update fields if provided
        if (request.getAmount() != null) {
            loan.setAmount(request.getAmount());
            // Update remainingBalance only if no repayments have been made
            if (loan.getRepayments().isEmpty()) {
                loan.setRemainingBalance(request.getAmount());
            }
        }
        if (request.getInterestRate() != null) {
            loan.setInterestRate(request.getInterestRate());
        }
        if (request.getStatus() != null) {
            loan.setStatus(request.getStatus());
        }

        // Save the updated loan
        Loan updatedLoan = loanRepository.save(loan);
        logger.info("Loan updated successfully for user: {}", user.getUsername());

        LoanDTO loanDTO = convertToDTO(updatedLoan);
        return ResponseEntity.ok(new LoanResponse("success", "Loan updated successfully", loanDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLoan(@PathVariable Long id) {
        logger.info("Deleting loan with id: {}", id);

        // Get the authenticated user
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        logger.debug("Authenticated user: {}", user.getUsername());

        // Fetch the loan
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        logger.debug("Loan user: {}", loan.getUser() != null ? loan.getUser().getUsername() : "null");

        // Authorization check with debugging
        if (loan.getUser() == null || !loan.getUser().getUsername().equals(user.getUsername())) {
            logger.warn("Unauthorized access attempt: Loan user ({}) does not match authenticated user ({})",
                    loan.getUser() != null ? loan.getUser().getUsername() : "null", user.getUsername());
            return ResponseEntity.status(403).body(new ErrorResponse("error", "Unauthorized to delete this loan"));
        }

        // Delete the loan (cascading will remove associated repayments)
        loanRepository.delete(loan);
        logger.info("Loan deleted successfully for user: {}", user.getUsername());

        return ResponseEntity.ok(new LoanResponse("success", "Loan deleted successfully", null));
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setId(loan.getId());
        loanDTO.setAmount(loan.getAmount());
        loanDTO.setInterestRate(loan.getInterestRate());
        loanDTO.setStatus(loan.getStatus());
        loanDTO.setRemainingBalance(loan.getRemainingBalance());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(loan.getUser().getId());
        userDTO.setUsername(loan.getUser().getUsername());
        userDTO.setEmail(loan.getUser().getEmail());
        loanDTO.setUser(userDTO);

        loanDTO.setRepayments(loan.getRepayments().stream().map(repayment -> {
            RepaymentDTO repaymentDTO = new RepaymentDTO();
            repaymentDTO.setId(repayment.getId());
            repaymentDTO.setAmount(repayment.getAmount());
            repaymentDTO.setRepaymentDate(repayment.getRepaymentDate());
            return repaymentDTO;
        }).collect(Collectors.toList()));

        return loanDTO;
    }
}

class LoanRequest {
    private Double amount;
    private Double interestRate;
    private String status;

    // Getters and setters
    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

class LoanResponse {
    private String status;
    private String message;
    private LoanDTO loan;

    public LoanResponse(String status, String message, LoanDTO loan) {
        this.status = status;
        this.message = message;
        this.loan = loan;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LoanDTO getLoan() {
        return loan;
    }
}

class ErrorResponse {
    private String status;
    private String message;

    public ErrorResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}