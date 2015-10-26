package ch.hsr.whitespace.javapilot.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpeedTest {

	@Test
	public void testMaxSpeed() {
		Power speed = new Power(230);
		assertEquals(255, speed.increase(40).getValue());
	}

	@Test
	public void testMinSpeed() {
		Power speed = new Power(30);
		assertEquals(0, speed.reduce(40).getValue());
	}

	@Test
	public void testAccelerate() {
		Power speed = new Power(120);
		assertEquals(160, speed.increase(40).getValue());
	}

	@Test
	public void testReduce() {
		Power speed = new Power(120);
		assertEquals(80, speed.reduce(40).getValue());
	}

}
