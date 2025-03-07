package emtech.loanManagement.repository;

import emtech.loanManagement.entity.Repayment;
import emtech.loanManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findByLoanUser(User user);
}