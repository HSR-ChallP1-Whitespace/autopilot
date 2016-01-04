package ch.hsr.whitespace.javapilot.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class CurveDrivingActor extends AbstractTrackPartDrivingActor {

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
		setPower(currentPower);
	}
}
