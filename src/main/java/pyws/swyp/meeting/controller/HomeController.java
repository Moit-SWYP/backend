package pyws.swyp.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pyws.swyp.meeting.controller.api.HomeApi;
import pyws.swyp.meeting.dto.HomeResponse;
import pyws.swyp.meeting.service.HomeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController implements HomeApi {

    private final HomeService homeService;

    @GetMapping
    public HomeResponse getHomeInfo(@AuthenticationPrincipal Long memberId) {
        return homeService.getHomeInfo(memberId);
    }
}
