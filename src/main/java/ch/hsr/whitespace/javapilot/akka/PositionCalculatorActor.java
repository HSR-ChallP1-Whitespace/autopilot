package ch.hsr.whitespace.javapilot.akka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuehlke.carrera.relayapi.messages.SensorEvent;
import com.zuehlke.carrera.timeseries.FloatingHistory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.events.TrackRecognitionFinished;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class PositionCalculatorActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(PositionCalculatorActor.class);

	private List<TrackPart> trackParts;
	private TrackPart currentTrackPart;
	private int currentTrackPartIndex = 0;
	FloatingHistory smoothedValues;

	public static Props props(ActorRef pilot) {
		return Props.create(PositionCalculatorActor.class, () -> new PositionCalculatorActor());
	}

	public PositionCalculatorActor() {
		smoothedValues = new FloatingHistory(8);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof TrackRecognitionFinished) {
			this.trackParts = ((TrackRecognitionFinished) message).getTrackParts();
			currentTrackPart = trackParts.get(currentTrackPartIndex);
		} else if (message instanceof SensorEvent && trackParts != null) {
			handleSensorEvent((SensorEvent) message);
		}
	}

	private void handleSensorEvent(SensorEvent message) {
		smoothedValues.shift(message.getG()[2]);
		if (hasDirectionChanged(smoothedValues.currentMean(), smoothedValues.currentStDev())) {
			incrementCurrentTrack();
			System.out.println("Current direction:" + getCurrentPositionString());
		}
	}

	private boolean hasDirectionChanged(double gyrzValue, double gyrzStdDev) {
		Direction newDirection = Direction.getNewDirection(currentTrackPart.getDirection(), gyrzValue, gyrzStdDev);
		return newDirection != currentTrackPart.getDirection();
	}

	private void incrementCurrentTrack() {
		if (currentTrackPartIndex == (trackParts.size() - 1))
			currentTrackPartIndex = 0;
		else
			currentTrackPartIndex++;
		currentTrackPart = trackParts.get(currentTrackPartIndex);
	}

	private String getCurrentPositionString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-- ");
		for (int i = 0; i < trackParts.size(); i++) {
			if (i == currentTrackPartIndex)
				sb.append((char) 27 + "[33m");
			sb.append(getDirectionShortCut(trackParts.get(i).getDirection()));
			sb.append(" (" + i + ")");
			if (i == currentTrackPartIndex)
				sb.append((char) 27 + "[0m");
			sb.append(" -- ");
		}
		return sb.toString();
	}

	private String getDirectionShortCut(Direction direction) {
		switch (direction) {
		case LEFT:
			return "L";
		case RIGHT:
			return "R";
		default:
			return "S";
		}
	}
}
