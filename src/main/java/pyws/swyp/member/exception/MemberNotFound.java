package pyws.swyp.member.exception;

import static pyws.swyp.global.error.ErrorCode.MEMBER_NOT_FOUND;

import pyws.swyp.global.error.CustomException;

public class MemberNotFound extends CustomException {

    public MemberNotFound() {
        super(MEMBER_NOT_FOUND);
    }
}
