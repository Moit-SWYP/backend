package pyws.swyp.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;

@Service
@RequiredArgsConstructor
public class MemberWithdrawCleanupService {

    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DateVoteRepository dateVoteRepository;
    private final TimeVoteRepository timeVoteRepository;

    @Transactional
    public void cleanup(Long memberId) {
        List<Long> participantIds = meetingParticipantRepository.findIdsByMemberId(memberId);
        if (participantIds.isEmpty()) {
            return;
        }

        dateVoteRepository.deleteAllByParticipantIds(participantIds);
        timeVoteRepository.deleteAllByParticipantIds(participantIds);
        meetingParticipantRepository.deleteAllByIds(participantIds);
    }
}

