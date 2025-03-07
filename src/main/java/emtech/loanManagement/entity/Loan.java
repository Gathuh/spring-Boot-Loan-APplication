package emtech.loanManagement.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Double remainingBalance; // New field to track remaining balance

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Repayment> repayments = new ArrayList<>(); // New relationship to repayments

    // Default constructor
    public Loan() {
        this.status = "PENDING";
        this.remainingBalance = 0.0; // Will be set to the loan amount upon creation
    }
}