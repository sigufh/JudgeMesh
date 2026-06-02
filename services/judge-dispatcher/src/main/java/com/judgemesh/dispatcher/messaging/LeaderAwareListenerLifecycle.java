package com.judgemesh.dispatcher.messaging;

import com.judgemesh.dispatcher.service.LeaderElectionService;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RabbitListenerEndpointRegistry.class)
public class LeaderAwareListenerLifecycle implements SmartInitializingSingleton, LeaderElectionService.LeadershipListener {
    private static final String LISTENER_ID = "judgeTaskListener";

    private final LeaderElectionService leaderElectionService;
    private final RabbitListenerEndpointRegistry registry;

    public LeaderAwareListenerLifecycle(
            LeaderElectionService leaderElectionService,
            RabbitListenerEndpointRegistry registry) {
        this.leaderElectionService = leaderElectionService;
        this.registry = registry;
    }

    @Override
    public void afterSingletonsInstantiated() {
        leaderElectionService.addListener(this);
        onLeadershipChanged(leaderElectionService.isLeader(), leaderElectionService.status().get("mode").toString());
    }

    @Override
    public void onLeadershipChanged(boolean leader, String mode) {
        var container = registry.getListenerContainer(LISTENER_ID);
        if (container == null) {
            return;
        }
        if (leader) {
            if (!container.isRunning()) {
                container.start();
            }
        } else if (container.isRunning()) {
            container.stop();
        }
    }
}
