package ch.hsr.whitespace.javapilot.akka.messages;

import java.util.List;

public class CheckedPatternsMessage {

	private List<String> checkedPatterns;

	public CheckedPatternsMessage(List<String> checkedPatterns) {
		super();
		this.checkedPatterns = checkedPatterns;
	}

	public List<String> getCheckedPatterns() {
		return checkedPatterns;
	}

}
