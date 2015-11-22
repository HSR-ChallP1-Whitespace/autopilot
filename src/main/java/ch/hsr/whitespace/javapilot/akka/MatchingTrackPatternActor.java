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
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;
import ch.hsr.whitespace.javapilot.util.StringUtil;

public class MatchingTrackPatternActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(MatchingTrackPatternActor.class);

	private PossibleTrackMatch match;
	private Iterator<RecognitionTrackPart> trackPartIterator;
	private boolean matchFailed = false;

	public MatchingTrackPatternActor(PossibleTrackMatch trackMatch, Direction currentDirection) {
		this.match = trackMatch;
		this.trackPartIterator = match.getTrackParts().iterator();
		handleNewDirection(currentDirection);
	}

	public static Props props(ActorRef pilot, PossibleTrackMatch trackMatch, Direction currentDirection) {
		return Props.create(MatchingTrackPatternActor.class, () -> new MatchingTrackPatternActor(trackMatch, currentDirection));
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

		RecognitionTrackPart nextPart = trackPartIterator.next();
		if (!isNextDirectionCorrect(newDirection, nextPart)) {
			LOGGER.warn("The pattern " + StringUtil.getPatternString(match.getTrackParts()) + " is not correct...");
			sendResponseMessage(false);
		}
	}

	private void sendResponseMessage(boolean patternConfirmed) {
		getContext().parent().tell(new MatchingTrackPatternResponseMessage(match, patternConfirmed), getSelf());
	}

	private boolean isNextDirectionCorrect(Direction newDirection, RecognitionTrackPart nextPart) {
		return newDirection == nextPart.getDirection();
	}

}