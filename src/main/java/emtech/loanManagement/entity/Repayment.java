package emtech.loanManagement.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(name = "payment_date") // Match the database column name
    private LocalDateTime repaymentDate;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Loan getLoan() { return loan; }
    public void setLoan(Loan loan) { this.loan = loan; }
    public LocalDateTime getRepaymentDate() { return repaymentDate; }
    public void setRepaymentDate(LocalDateTime repaymentDate) { this.repaymentDate = repaymentDate; }
}