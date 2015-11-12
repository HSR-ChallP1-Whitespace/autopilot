package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.model.track.Direction;

public class DirectionChangeRecognizerActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(DirectionChangeRecognizerActor.class);

	private FloatingHistory smoothedValues;
	private Direction currentDirection;

	public DirectionChangeRecognizerActor() {
		smoothedValues = new FloatingHistory(3);
	}

	public static Props props(ActorRef pilot) {
		return Props.create(DirectionChangeRecognizerActor.class, () -> new DirectionChangeRecognizerActor());
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof SensorEvent) {
			handleSensorEvent((SensorEvent) message);
		}
	}

	private void handleSensorEvent(SensorEvent message) {
		smoothedValues.shift(message.getG()[2]);
		Direction newDirection = Direction.getNewDirection(currentDirection, smoothedValues.currentMean(), smoothedValues.currentStDev());
		if (hasDirectionChanged(newDirection)) {
			currentDirection = newDirection;
			// LOGGER.info("Direction has changed: " + newDirection);
			sendDirectionChangeMessage(message.getTimeStamp());
		}
	}

	private void sendDirectionChangeMessage(long timeStamp) {
		getContext().parent().tell(new DirectionChangedMessage(timeStamp, currentDirection), getSelf());

	}

	private boolean hasDirectionChanged(Direction newDirection) {
		return newDirection != currentDirection;
	}

}