package pyws.swyp.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pyws.swyp.meeting.repository.MeetingParticipantRepository;
import pyws.swyp.meeting.repository.MeetingRecordRepository;
import pyws.swyp.meeting.repository.RecordImageRepository;
import pyws.swyp.meeting.repository.vote.DateVoteRepository;
import pyws.swyp.meeting.repository.vote.TimeVoteRepository;
import pyws.swyp.member.repository.friend.FriendGroupMemberRepository;
import pyws.swyp.member.repository.friend.FriendGroupRepository;
import pyws.swyp.member.repository.friend.FriendshipRepository;

@Service
@RequiredArgsConstructor
public class MemberWithdrawCleanupService {

    private final MeetingParticipantRepository meetingParticipantRepository;
    private final DateVoteRepository dateVoteRepository;
    private final TimeVoteRepository timeVoteRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendGroupMemberRepository friendGroupMemberRepository;
    private final MeetingRecordRepository meetingRecordRepository;
    private final RecordImageRepository recordImageRepository;

    @Transactional
    public void cleanup(Long memberId) {
        List<Long> participantIds = meetingParticipantRepository.findIdsByMemberId(memberId);
        if (participantIds.isEmpty()) {
            return;
        }

        List<Long> reviewIds = meetingRecordRepository.findIdsByMemberId(memberId);
        if (!reviewIds.isEmpty()) {
            recordImageRepository.deleteAllByRecordIds(reviewIds);
        }

        meetingRecordRepository.deleteAllByMemberId(memberId);
        dateVoteRepository.deleteAllByParticipantIds(participantIds);
        timeVoteRepository.deleteAllByParticipantIds(participantIds);
        meetingParticipantRepository.deleteAllByIds(participantIds);

        friendshipRepository.deleteAllByMemberIds(memberId);
        List<Long> groupIds = friendGroupRepository.findIdsByMemberId(memberId);
        if (groupIds.isEmpty()) {
            return;
        }
        friendGroupMemberRepository.deleteAllByGroupIds(groupIds);
        friendGroupRepository.deleteAllByIds(groupIds);
    }
}

