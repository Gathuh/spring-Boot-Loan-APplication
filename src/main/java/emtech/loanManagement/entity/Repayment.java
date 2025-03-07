package emtech.loanManagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "repayments")
@Data
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonBackReference
    private Loan loan;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    public Repayment() {
        this.paymentDate = LocalDateTime.now();
    }

    // Parameterized constructor for convenience
    public Repayment(Double amount, Loan loan) {
        this.amount = amount;
        this.loan = loan;
        this.paymentDate = LocalDateTime.now();
    }
}