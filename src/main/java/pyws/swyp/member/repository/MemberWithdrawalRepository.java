package pyws.swyp.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pyws.swyp.member.entity.MemberWithdrawal;

public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {
}

