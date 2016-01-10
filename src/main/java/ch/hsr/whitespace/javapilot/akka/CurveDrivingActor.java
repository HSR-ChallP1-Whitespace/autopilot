package ch.hsr.whitespace.javapilot.akka;

import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import ch.hsr.whitespace.javapilot.akka.messages.TrackPartEnteredMessage;
import ch.hsr.whitespace.javapilot.model.Power;
import ch.hsr.whitespace.javapilot.model.track.Direction;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;
import scala.concurrent.duration.Duration;

public class CurveDrivingActor extends AbstractTrackPartDrivingActor {

	// 0.5 = ca. middle of curve (based on last time in curve)
	private static final double START_NEXT_STRAIGHT_TIME_PERCENTAGE = 0.5;

	private static final int CURVE_POWER_INCREASE = 5;

	public static Props props(ActorRef pilot, TrackPart trackPart, int currentPower) {
		return Props.create(CurveDrivingActor.class, () -> new CurveDrivingActor(pilot, trackPart, currentPower));
	}

	public CurveDrivingActor(ActorRef pilot, TrackPart trackPart, int currentPower) {
		super(pilot, trackPart, currentPower);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		super.onReceive(message);
	}

	@Override
	protected void evaluateAndSetNewPower() {
		if (iAmSpeedingUp)
			currentPower = new Power(currentPower.getValue() + CURVE_POWER_INCREASE);
		setPower(currentPower);
	}

	@Override
	protected void enterTrackPart(TrackPartEnteredMessage message) {
		// if the next part is STRAIGHT, we want to give him control earlier
		// (speedup already in the curve)
		if (isNextPartStraight())
			scheduleStraightPartDriving();
		super.enterTrackPart(message);
	}

	private void scheduleStraightPartDriving() {
		long delayInMillis = (long) (trackPart.getDuration() * START_NEXT_STRAIGHT_TIME_PERCENTAGE);
		getContext().system().scheduler().scheduleOnce(Duration.create(delayInMillis, TimeUnit.MILLISECONDS), new Runnable() {
			@Override
			public void run() {
				stopDriving();
				tellNextTrackPartToDrive();
			}
		}, getContext().dispatcher());
	}

	/**
	 * checks if after this curve is a straight part...
	 */
	private boolean isNextPartStraight() {
		return this.nextTrackPart.getDirection() == Direction.STRAIGHT;
	}
}
