package ch.hsr.whitespace.javapilot.akka;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import ch.hsr.whitespace.javapilot.akka.messages.ConfirmTrackMatchMessage;
import ch.hsr.whitespace.javapilot.akka.messages.DirectionChangedMessage;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.recognition.RecognitionTrackPart;
import ch.hsr.whitespace.javapilot.model.track.recognition.matching.PossibleTrackMatch;

public class MatchingTrackPatternActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(MatchingTrackPatternActor.class);

	private PossibleTrackMatch match;
	private Iterator<RecognitionTrackPart> trackPartIterator;
	private boolean matchFailed = false;

	public MatchingTrackPatternActor(PossibleTrackMatch trackMatch) {
		this.match = trackMatch;
		this.trackPartIterator = match.getTrackParts().iterator();
		this.trackPartIterator.next();
	}

	public static Props props(ActorRef pilot, PossibleTrackMatch trackMatch) {
		return Props.create(MatchingTrackPatternActor.class, () -> new MatchingTrackPatternActor(trackMatch));
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof DirectionChangedMessage) {
			handleDirectionChanged((DirectionChangedMessage) message);
		}
	}

	private void handleDirectionChanged(DirectionChangedMessage message) {
		if (matchFailed)
			return;
		if (!trackPartIterator.hasNext()) {
			handleCorrectMatch();
			return;
		}

		RecognitionTrackPart nextPart = trackPartIterator.next();
		if (!isNextDirectionCorrect(message.getNewDirection(), nextPart)) {
			LOGGER.warn("The pattern " + match + " is not correct...");
			matchFailed = true;
			stopMySelf();
		}
	}

	private void handleCorrectMatch() {
		getContext().parent().tell(new ConfirmTrackMatchMessage(match), getSelf());
		stopMySelf();
	}

	private void stopMySelf() {
		getContext().stop(getSelf());
	}

	private boolean isNextDirectionCorrect(Direction newDirection, RecognitionTrackPart nextPart) {
		return newDirection == nextPart.getDirection();
	}

}