package ch.hsr.whitespace.javapilot.akka;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.akka.messages.MatchingTrackPatternResponseMessage;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import ch.hsr.whitespace.javapilot.model.track.matching.PossibleTrackMatch;
import ch.hsr.whitespace.javapilot.util.StringUtil;

public class MatchingTrackPatternActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(MatchingTrackPatternActor.class);

	private ActorRef trackRecognizer;
	private PossibleTrackMatch match;
	private Iterator<TrackPart> trackPartIterator;
	private boolean matchFailed = false;

	public MatchingTrackPatternActor(ActorRef trackRecognizer, PossibleTrackMatch trackMatch, Direction currentDirection) {
		this.trackRecognizer = trackRecognizer;
		this.match = trackMatch;
		this.trackPartIterator = match.getTrackParts().iterator();
		handleNewDirection(currentDirection);
	}

	public static Props props(ActorRef trackRecognizer, PossibleTrackMatch trackMatch, Direction currentDirection) {
		return Props.create(MatchingTrackPatternActor.class, () -> new MatchingTrackPatternActor(trackRecognizer, trackMatch, currentDirection));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DirectionChangedMessage) {
			handleDirectionChanged((DirectionChangedMessage) message);
		}
	}

	private void handleDirectionChanged(DirectionChangedMessage message) {
		handleNewDirection(message.getNewDirection());
	}

	private void handleNewDirection(Direction newDirection) {
		if (matchFailed)
			return;
		if (!trackPartIterator.hasNext()) {
			sendResponseMessage(true);
			return;
		}

		TrackPart nextPart = trackPartIterator.next();
		if (!isNextDirectionCorrect(newDirection, nextPart)) {
			LOGGER.warn("The pattern " + StringUtil.getPatternString(match.getTrackParts()) + " is not correct...");
			sendResponseMessage(false);
		}
	}

	private void sendResponseMessage(boolean patternConfirmed) {
		trackRecognizer.tell(new MatchingTrackPatternResponseMessage(match, patternConfirmed), getSelf());
	}

	private boolean isNextDirectionCorrect(Direction newDirection, TrackPart nextPart) {
		return newDirection == nextPart.getDirection();
	}

}