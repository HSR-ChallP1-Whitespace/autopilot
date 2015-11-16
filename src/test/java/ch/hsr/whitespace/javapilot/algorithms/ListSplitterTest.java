package ch.hsr.whitespace.javapilot.algorithms;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.hsr.whitespace.javapilot.algorithms.ListSplitter;
import ch.hsr.whitespace.javapilot.algorithms.ListSplitter.DestinationListsMustBeEmptyException;

public class ListSplitterTest {

	private ListSplitter splitter;

	public ListSplitterTest() {
		this.splitter = new ListSplitter();
	}

	@Test
	public void testWithNotEqualLengthLists() {
		List<String> sourceList = new ArrayList<>();
		List<String> destinationList1 = new ArrayList<>();
		List<String> destinationList2 = new ArrayList<>();
		sourceList.add("One");
		sourceList.add("Two");
		sourceList.add("Three");

		splitter.splitListIntoTwoParts(sourceList, destinationList1, destinationList2);

		assertEquals(1, destinationList1.size());
		assertEquals(2, destinationList2.size());
	}

	@Test(expected = DestinationListsMustBeEmptyException.class)
	public void testWithNotEmptyDestination1() {
		List<String> sourceList = new ArrayList<>();
		sourceList.add("One");
		sourceList.add("Two");

		List<String> destinationList1 = new ArrayList<>();
		destinationList1.add("test");

		splitter.splitListIntoTwoParts(sourceList, destinationList1, new ArrayList<String>());
	}

	@Test(expected = DestinationListsMustBeEmptyException.class)
	public void testWithNotEmptyDestination2() {
		List<String> sourceList = new ArrayList<>();
		sourceList.add("One");
		sourceList.add("Two");

		List<String> destinationList2 = new ArrayList<>();
		destinationList2.add("test");

		splitter.splitListIntoTwoParts(sourceList, new ArrayList<String>(), destinationList2);
	}

	@Test
	public void testWithEmptyList() {
		List<String> sourceList = new ArrayList<>();
		List<String> destinationList1 = new ArrayList<>();
		List<String> destinationList2 = new ArrayList<>();

		splitter.splitListIntoTwoParts(sourceList, destinationList1, destinationList2);

		assertEquals(0, destinationList1.size());
		assertEquals(0, destinationList2.size());
	}

	@Test
	public void testWithCorrectList() {
		List<String> sourceList = new ArrayList<>();
		sourceList.add("One");
		sourceList.add("Two");
		sourceList.add("Three");
		sourceList.add("Four");

		List<String> destinationList1 = new ArrayList<>();
		List<String> destinationList2 = new ArrayList<>();

		splitter.splitListIntoTwoParts(sourceList, destinationList1, destinationList2);

		assertEquals(2, destinationList1.size());
		assertEquals(2, destinationList2.size());

		assertEquals("One", destinationList1.get(0));
		assertEquals("Two", destinationList1.get(1));
		assertEquals("Three", destinationList2.get(0));
		assertEquals("Four", destinationList2.get(1));
	}
}
