package ch.hsr.whitespace.javapilot.akka.messages;

import akka.actor.ActorRef;
import ch.hsr.whitespace.javapilot.model.track.TrackPart;

public class ChainTrackPartActorsMessage {

	private TrackPart previousTrackPart;
	private ActorRef previousTrackPartActorRef;
	private TrackPart nextTrackPart;
	private ActorRef nextTrackPartActorRef;

	public ChainTrackPartActorsMessage(TrackPart previousTrackPart, ActorRef previousTrackPartActorRef, TrackPart nextTrackPart, ActorRef nextTrackPartActorRef) {
		this.nextTrackPart = nextTrackPart;
		this.nextTrackPartActorRef = nextTrackPartActorRef;
		this.previousTrackPart = previousTrackPart;
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

	public TrackPart getPreviousTrackPart() {
		return previousTrackPart;
	}

	public void setPreviousTrackPart(TrackPart previousTrackPart) {
		this.previousTrackPart = previousTrackPart;
	}

	public TrackPart getNextTrackPart() {
		return nextTrackPart;
	}

	public void setNextTrackPart(TrackPart nextTrackPart) {
		this.nextTrackPart = nextTrackPart;
	}

}
