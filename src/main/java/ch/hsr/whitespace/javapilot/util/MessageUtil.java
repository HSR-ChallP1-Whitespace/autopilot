package ch.hsr.whitespace.javapilot.util;

public class MessageUtil {

	public static boolean isMessageForwardNeeded(Object message, Class<?>[] neededTypes) {
		for (Class<?> neededType : neededTypes) {
			if (message.getClass().equals(neededType))
				return true;
		}
		return false;
	}

}
