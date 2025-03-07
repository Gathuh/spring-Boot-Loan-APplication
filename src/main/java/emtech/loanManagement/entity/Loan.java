package emtech.loanManagement.entity;

import jakarta.persistence.*;
import lombok.Data;

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
    private String status; // e.g., "PENDING", "APPROVED", "REPAID"

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who requested the loan
}