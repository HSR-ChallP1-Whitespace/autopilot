package ch.hsr.whitespace.javapilot.akka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class TrackPartDrivingActor extends UntypedActor {

	private final Logger LOGGER = LoggerFactory.getLogger(TrackPartDrivingActor.class);

	public static Props props(ActorRef pilot) {
		return Props.create(TrackPartDrivingActor.class, () -> new TrackPartDrivingActor());
	}

	public TrackPartDrivingActor() {
	}

	@Override
	public void onReceive(Object message) throws Exception {
	}

}
