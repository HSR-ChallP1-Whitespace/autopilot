package ch.hsr.whitespace.javapilot.algorithms;

import java.util.List;

public class ListSplitter {

	/**
	 * Splits list into two lists with same size.
	 * 
	 * @param source
	 *            Source List, has to be divisible through 2.
	 * @param destination1
	 *            Destination List one.
	 * @param destination2
	 *            Destination List two.
	 * @throws RuntimeException
	 */
	public <T> void splitListIntoTwoParts(List<T> source, List<T> destination1, List<T> destination2) throws RuntimeException {
		if (!destination1.isEmpty() || !destination2.isEmpty())
			throw new DestinationListsMustBeEmptyException();

		for (int i = 0; i < (source.size() / 2); i++) {
			destination1.add(source.get(i));
		}
		for (int i = (source.size() / 2); i < source.size(); i++) {
			destination2.add(source.get(i));
		}
	}

	public class DestinationListsMustBeEmptyException extends RuntimeException {
		private static final long serialVersionUID = 4834852855654339387L;

		public DestinationListsMustBeEmptyException() {
			super("The destination lists have to be empty!");
		}
	}

}
