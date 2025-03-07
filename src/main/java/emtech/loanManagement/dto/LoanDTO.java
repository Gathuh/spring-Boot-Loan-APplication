package emtech.loanManagement.dto;

import java.util.List;

public class LoanDTO {
    private Long id;
    private Double amount;
    private Double interestRate;
    private String status;
    private Double remainingBalance;
    private UserDTO user;
    private List<RepaymentDTO> repayments;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public List<RepaymentDTO> getRepayments() {
        return repayments;
    }

    public void setRepayments(List<RepaymentDTO> repayments) {
        this.repayments = repayments;
    }
}