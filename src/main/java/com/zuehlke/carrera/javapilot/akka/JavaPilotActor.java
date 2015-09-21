package com.zuehlke.carrera.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import com.zuehlke.carrera.javapilot.akka.experimental.ThresholdConfiguration;
import com.zuehlke.carrera.javapilot.config.PilotProperties;
import com.zuehlke.carrera.javapilot.services.EndpointAnnouncement;
import com.zuehlke.carrera.javapilot.services.PilotToRelayConnection;
import com.zuehlke.carrera.relayapi.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 *  Central actor responsible for driving the car. All data gets here and all decisions are finally made here.
 */
public class JavaPilotActor extends UntypedActor {

    private final Logger LOGGER = LoggerFactory.getLogger(JavaPilotActor.class);
    private final PilotProperties properties;

    private ActorRef sensorEntryPoint;
    private ActorRef velocityEntryPoint;
    private ActorRef penaltyEntryPoint;

    private PilotToRelayConnection relayConnection;

    public JavaPilotActor(PilotProperties properties) {

        this.properties = properties;

        createTopology ();
    }

    private void createTopology() {
        Map<String, ActorRef> entryPoints = new PilotTopology(getSelf(), getContext().system()).create();

        this.sensorEntryPoint = entryPoints.get(PilotTopology.SENSOR_ENTRYPOINT);
        this.velocityEntryPoint = entryPoints.get(PilotTopology.VELOCITY_ENTRYPOINT);
        this.penaltyEntryPoint = entryPoints.get(PilotTopology.PENALTY_ENTRYPOINT);
    }


    public static Props props ( PilotProperties properties ) {
        return Props.create(new Creator<JavaPilotActor>() {
            private static final long serialVersionUID = 1L;

            @Override
            public JavaPilotActor create() throws Exception {
                return new JavaPilotActor( properties );
            }
        });
    }


    @Override
    public void onReceive(Object message) throws Exception {

        try {

            if (message instanceof RaceStartMessage) {
                handleRaceStart((RaceStartMessage) message);

            } else if (message instanceof RaceStopMessage) {
                handleRaceStop((RaceStopMessage) message);

            } else if (message instanceof SensorEvent) {
                handleSensorEvent((SensorEvent) message);

            } else if (message instanceof VelocityMessage) {
                handleVelocityMessage((VelocityMessage) message);

            } else if (message instanceof PilotToRelayConnection) {
                this.relayConnection = (PilotToRelayConnection) message;

            } else if (message instanceof EndpointAnnouncement) {
                handleEndpointAnnouncement((EndpointAnnouncement) message);

            } else if (message instanceof PowerAction) {
                handlePowerAction(((PowerAction) message).getPowerValue());

            } else if (message instanceof PenaltyMessage ) {
                handlePenaltyMessage ((PenaltyMessage) message );

            } else if ( message instanceof ThresholdConfiguration) {
                sensorEntryPoint.forward(message, getContext());

            } else if ( message instanceof RoundTimeMessage ) {
                handleRoundTime((RoundTimeMessage) message);

            } else if (message instanceof String) {

                // simply ignore this if there is no connection.
                if ("ENSURE_CONNECTION".equals(message)) {
                    if (relayConnection != null) {
                        relayConnection.ensureConnection();
                    }
                }
            } else {
                unhandled(message);
            }
        } catch ( Exception e ) {
            LOGGER.error("Caught exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void handleRoundTime(RoundTimeMessage message) {
        LOGGER.info ( "Round Time in ms: " + message.getRoundDuration());
    }

    private void handlePenaltyMessage(PenaltyMessage message) {
        penaltyEntryPoint.forward(message, getContext());
    }

    /**
     * Action request from the processing topology
     * @param powerValue the new power value to be requested on the track
     */
    private void handlePowerAction(int powerValue) {
        long now = System.currentTimeMillis();
            relayConnection.send(new PowerControl(powerValue, properties.getName(),
                    properties.getAccessCode(), now));
    }

    private void handleEndpointAnnouncement(EndpointAnnouncement message) {
        if ( relayConnection != null ) {
            relayConnection.announce(message.getUrl());
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        if ( message.getVelocity() == -999 ) {
            handleSample(message);
        } else {
            velocityEntryPoint.forward(message, getContext());
        }
    }

    private void handleSensorEvent(SensorEvent message) {
        if ( isSample ( message ) ) {
            handleSample(message);
        } else {
            sensorEntryPoint.forward(message, getContext());
        }
    }

    private boolean isSample(SensorEvent message) {
        return (( message.getM()[0] == 111.0f)
                && ( message.getM()[1] == 112.0f )
                && ( message.getM()[2] == 113.0f ));
    }

    /**
     * log the receipt and answer with a Speedcontrol of 0;
     * @param message the sample event
     */
    private void handleSample(SensorEvent message) {
        LOGGER.info("received sample SensorEvent: " + message.toString());
        long now = System.currentTimeMillis();
        relayConnection.send (new PowerControl(0, properties.getName(), properties.getAccessCode(), now));
    }

    /**
     * log the receipt and answer with a Speedcontrol of 0;
     * @param message the sample velocity
     */
    private void handleSample(VelocityMessage message) {
        LOGGER.info("received sample velocity message: " + message.toString());
        long now = System.currentTimeMillis();
        relayConnection.send (new PowerControl(0, properties.getName(), properties.getAccessCode(), now));
    }


    private void handleRaceStop(RaceStopMessage message) {
        LOGGER.info("received race stop");
    }

    private void handleRaceStart(RaceStartMessage message) {
        createTopology();
        long now = System.currentTimeMillis();
        LOGGER.info("received race start");
    }
}
