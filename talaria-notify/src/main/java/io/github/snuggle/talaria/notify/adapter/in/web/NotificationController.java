package io.github.snuggle.talaria.notify.adapter.in.web;

import io.github.snuggle.talaria.common.dto.ApiResponse;
import io.github.snuggle.talaria.notify.application.port.in.GetNotificationQuery;
import io.github.snuggle.talaria.notify.application.port.in.SendNotificationUseCase;
import io.github.snuggle.talaria.notify.domain.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final GetNotificationQuery getNotificationQuery;

    public NotificationController(
        SendNotificationUseCase sendNotificationUseCase,
        GetNotificationQuery getNotificationQuery
    ) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.getNotificationQuery = getNotificationQuery;
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(
        @RequestBody SendNotificationUseCase.SendNotificationCommand command
    ) {
        sendNotificationUseCase.sendManual(command);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<Notification>>> getRecent(
        @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(getNotificationQuery.findRecent(limit)));
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<ApiResponse<Notification>> getById(@PathVariable Long id) {
        return getNotificationQuery.findById(id)
            .map(n -> ResponseEntity.ok(ApiResponse.ok(n)))
            .orElse(ResponseEntity.notFound().build());
    }
}
