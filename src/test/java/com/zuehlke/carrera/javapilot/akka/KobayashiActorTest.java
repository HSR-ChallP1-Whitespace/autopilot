package com.zuehlke.carrera.javapilot.akka;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.zuehlke.carrera.javapilot.config.PilotProperties;
import com.zuehlke.carrera.javapilot.services.PilotToRelayConnection;
import com.zuehlke.carrera.relayapi.messages.PowerControl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class KobayashiActorTest {

    private AtomicInteger speed = new AtomicInteger();

    @Test
    @Ignore
    public void testThresholdConfiguration () throws InterruptedException {

        PilotProperties properties = new PilotProperties();

        ActorSystem system = ActorSystem.create("testSystem");

        ActorRef pilot = system.actorOf(JavaPilotActor.props(properties));

        Thread.sleep(1000);

        pilot.tell(new PilotToRelayConnection() {
            @Override
            public void announce(String optionalUrl) {
            }

            @Override
            public void send(PowerControl powerControl) {
                speed.set(powerControl.getP());
            }

            @Override
            public void ensureConnection() {
            }
        }, ActorRef.noSender());

        Assert.fail("Not implemented properly yet");
    }

}
