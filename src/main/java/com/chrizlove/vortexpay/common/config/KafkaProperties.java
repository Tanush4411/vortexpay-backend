package com.chrizlove.vortexpay.common.config;

import com.chrizlove.vortexpay.common.enums.EventAggregateType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.kafka")
@Getter
@Setter
public class KafkaProperties {

    private Map<String,String> topics=new HashMap<>();

    public String topicFor(EventAggregateType aggregateType) {
        String topic=topics.get(aggregateType.name().toLowerCase());

        if(topic==null){
            throw new IllegalStateException("No kafka topic is configured for aggregateType " + aggregateType.name());
        }
        return topic;
    }
}
