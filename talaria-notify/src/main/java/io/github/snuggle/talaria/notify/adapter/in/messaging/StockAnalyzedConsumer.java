package io.github.snuggle.talaria.notify.adapter.in.messaging;

import io.github.snuggle.talaria.common.event.StockAnalyzedEvent;
import io.github.snuggle.talaria.notify.application.port.in.SendNotificationUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockAnalyzedConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockAnalyzedConsumer.class);

    private final SendNotificationUseCase sendNotificationUseCase;

    public StockAnalyzedConsumer(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    @KafkaListener(
        topics = "talaria.stock.analyzed",
        groupId = "talaria-notify-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, StockAnalyzedEvent> record) {
        log.info("Kafka consume | topic={} | partition={} | offset={}",
                 record.topic(), record.partition(), record.offset());
        try {
            sendNotificationUseCase.sendFromEvent(record.value());
        } catch (Exception e) {
            log.error("알림 처리 실패 | offset={} | error={}", record.offset(), e.getMessage(), e);
            throw e; // Kafka가 재시도 처리
        }
    }
}
