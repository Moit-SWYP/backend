package pyws.swyp.member.controller;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.member.controller.api.TestApi;
import pyws.swyp.member.dto.TestRequest;
import pyws.swyp.member.dto.TestResponse;
import pyws.swyp.member.exception.MemberNotFound;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class TestController implements TestApi {

    @PostMapping("/test-success")
    public TestResponse test(@RequestBody @Validated TestRequest request) {
        return TestResponse.builder()
                .message("서버에서 받은 메시지: " + request.getName())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    @PostMapping("/test-fail")
    public TestResponse test2(@RequestBody @Validated TestRequest request) {
        throw new MemberNotFound();
    }
}
