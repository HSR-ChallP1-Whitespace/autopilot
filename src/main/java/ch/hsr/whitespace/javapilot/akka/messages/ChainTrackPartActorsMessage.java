package ch.hsr.whitespace.javapilot.akka.messages;

import akka.actor.ActorRef;

public class ChainTrackPartActorsMessage {

	private ActorRef previousTrackPartActorRef;
	private ActorRef nextTrackPartActorRef;

	public ChainTrackPartActorsMessage(ActorRef previousTrackPartActorRef, ActorRef nextTrackPartActorRef) {
		super();
		this.nextTrackPartActorRef = nextTrackPartActorRef;
		this.previousTrackPartActorRef = previousTrackPartActorRef;
	}

	public ActorRef getPreviousTrackPartActorRef() {
		return previousTrackPartActorRef;
	}

	public void setPreviousTrackPartActorRef(ActorRef previousTrackPartActorRef) {
		this.previousTrackPartActorRef = previousTrackPartActorRef;
	}

	public ActorRef getNextTrackPartActorRef() {
		return nextTrackPartActorRef;
	}

	public void setNextTrackPartActorRef(ActorRef nextTrackPartActorRef) {
		this.nextTrackPartActorRef = nextTrackPartActorRef;
	}

}
