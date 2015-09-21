package com.zuehlke.carrera.javapilot.akka.experimental;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.zuehlke.carrera.javapilot.akka.PowerAction;
import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.relayapi.messages.VelocityMessage;

/**
 *  A very simple actor that determines the power value by a configurable Threshold on any of the 10 observables
 */
public class ConstantPower extends UntypedActor {

    private ThresholdConfiguration configuration;
    private int power;
    private ActorRef pilot;

    public ConstantPower(ActorRef pilot, int power) {
        this.pilot = pilot;
        this.power = power;
    }

    public static Props props ( ActorRef pilot, int power ) {
        return Props.create( ConstantPower.class, ()->new ConstantPower( pilot, power ));
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if ( message instanceof SensorEvent ) {
            handleSensorEvent((SensorEvent) message);
        } else if ( message instanceof VelocityMessage) {
                handleVelocityMessage((VelocityMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void handleVelocityMessage(VelocityMessage message) {
        // ignore for now
    }

    private void handleSensorEvent(SensorEvent event) {
        pilot.tell ( new PowerAction(power), getSelf());
    }
}
