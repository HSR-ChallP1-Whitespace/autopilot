package ch.hsr.whitespace.javapilot.akka.messages;

public class PrintTrackPositionMessage {

	private int currentTrackPartId;

	public PrintTrackPositionMessage(int currentTrackPartId) {
		super();
		this.currentTrackPartId = currentTrackPartId;
	}

	public int getCurrentTrackPartId() {
		return currentTrackPartId;
	}

	public void setCurrentTrackPartId(int currentTrackPartId) {
		this.currentTrackPartId = currentTrackPartId;
	}

}
