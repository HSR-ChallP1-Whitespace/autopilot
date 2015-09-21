package com.zuehlke.carrera.javapilot.akka.experimental;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;

/**
 *  A very simple actor that determines the power value by a configurable Threshold on any of the 10 observables
 */
public class SimpleThresholdReaction extends UntypedActor {

    private ActorRef pilot;
    private ThresholdConfiguration configuration;

    public SimpleThresholdReaction(ActorRef pilot) {
        this.pilot = pilot;
    }

    public static Props props ( ActorRef pilot ) {
        return Props.create( SimpleThresholdReaction.class, ()->new SimpleThresholdReaction(pilot));
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if ( message instanceof SensorEvent ) {
            handleSensorEvent ((SensorEvent)message);
        } else if ( message instanceof ThresholdConfiguration ) {
            handleNewConfiguration ( (ThresholdConfiguration) message );
        } else {
            unhandled( message );
        }
    }

    private void handleSensorEvent(SensorEvent event) {

        if (configuration.isAboveThreshold ( event )) {
            pilot.tell(new PowerAction( configuration.getLowerPowerValue()), ActorRef.noSender());
        } else {
            pilot.tell(new PowerAction( configuration.getHigherPowerValue()), ActorRef.noSender());
        }

    }

    private void handleNewConfiguration(ThresholdConfiguration message) {
        this.configuration = message;
    }
}
