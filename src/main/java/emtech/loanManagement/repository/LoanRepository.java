package emtech.loanManagement.repository;

import emtech.loanManagement.entity.Loan;
import emtech.loanManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    @Query("SELECT l FROM Loan l LEFT JOIN FETCH l.repayments WHERE l.user = :user")
    List<Loan> findByUser(User user);
}