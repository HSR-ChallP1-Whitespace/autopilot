package com.zuehlke.carrera.javapilot.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.zuehlke.carrera.javapilot.akka.JavaPilotActor;
import com.zuehlke.carrera.javapilot.config.PilotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

/**
 * Manages the carrera pilot instance.
 */
@Service
@EnableScheduling
public class PilotService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PilotService.class);

    private final PilotProperties settings;

    private final ActorSystem system;
    private final ActorRef pilotActor;
    private final String endPointUrl;

    @Autowired
    public PilotService(PilotProperties settings, EndpointService endpointService,
                        SimulatorService simulatorService ){
        this.settings = settings;
        this.endPointUrl = endpointService.getHttpEndpoint();
        system = ActorSystem.create(settings.getName());
        pilotActor = system.actorOf(JavaPilotActor.props(settings));

        // Simulator learns about the pilot
        simulatorService.registerPilot(pilotActor);

        // Pilot learns about the simulator
        pilotActor.tell(new PilotToRaceTrackConnector(simulatorService.getSystem()), ActorRef.noSender());
    }



    //@PostConstruct
    public void connectToRelay () {
        PilotToRelayConnection pilotToRelayConnection = new PilotToRelayStompConnection(
                settings.getRelayUrl(),
                settings.getName(),
                "admin",
                "admin",
                (startMessage) -> pilotActor.tell(startMessage, pilotActor),
                (stopMessage) -> pilotActor.tell(stopMessage, pilotActor),
                (sensorEvent) -> pilotActor.tell(sensorEvent, pilotActor),
                (velocityMessage) -> pilotActor.tell(velocityMessage, pilotActor),
                (penaltyMessage) -> pilotActor.tell(penaltyMessage, pilotActor));

        pilotActor.tell(pilotToRelayConnection, ActorRef.noSender());
    }

    @Scheduled(fixedRate = 2000)
    public void ensureConnection() {
        pilotActor.tell("ENSURE_CONNECTION", ActorRef.noSender());
    }

    @Scheduled(fixedRate = 1000)
    public void announce() {
        pilotActor.tell(new EndpointAnnouncement(endPointUrl), ActorRef.noSender());
    }

    @PreDestroy
    public void shutdown () {
        LOGGER.info("Shutting down the actor system.");
        system.shutdown();

    }

    public void registerConnection(PilotToRelayConnection pilot) {
        if ( pilot != null ) {
            pilotActor.tell(pilot, ActorRef.noSender());
        }
    }

    public ActorRef getPilotActor() {
        return pilotActor;
    }
}
