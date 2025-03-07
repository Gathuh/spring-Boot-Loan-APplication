package emtech.loanManagement.repository;

import emtech.loanManagement.entity.Loan;
import emtech.loanManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
}