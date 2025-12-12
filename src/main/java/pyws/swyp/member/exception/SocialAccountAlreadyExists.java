package pyws.swyp.member.exception;

import static pyws.swyp.global.error.ErrorCode.SOCIAL_ACCOUNT_ALREADY_EXISTS;

import pyws.swyp.global.error.CustomException;

public class SocialAccountAlreadyExists extends CustomException {

    public SocialAccountAlreadyExists() {
        super(SOCIAL_ACCOUNT_ALREADY_EXISTS);
    }
}
