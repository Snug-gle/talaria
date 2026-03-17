package io.github.snuggle.talaria.notify.application.port.in;

import io.github.snuggle.talaria.notify.domain.model.Notification;
import io.github.snuggle.talaria.notify.domain.model.NotificationDispatch;

import java.util.List;
import java.util.Optional;

public interface GetNotificationQuery {

    Optional<Notification> findById(Long id);

    List<Notification> findRecent(int limit);

    List<NotificationDispatch> findDispatchesByNotificationId(Long notificationId);
}
