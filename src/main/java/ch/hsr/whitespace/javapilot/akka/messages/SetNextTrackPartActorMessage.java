package ch.hsr.whitespace.javapilot.akka.messages;

import akka.actor.ActorRef;

public class SetNextTrackPartActorMessage {

	private ActorRef nextTrackPartActorRef;

	public SetNextTrackPartActorMessage(ActorRef nextTrackPartActorRef) {
		super();
		this.nextTrackPartActorRef = nextTrackPartActorRef;
	}

	public ActorRef getNextTrackPartActorRef() {
		return nextTrackPartActorRef;
	}

	public void setNextTrackPartActorRef(ActorRef nextTrackPartActorRef) {
		this.nextTrackPartActorRef = nextTrackPartActorRef;
	}

}
