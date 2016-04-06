package com.github.fommil.ff.ui;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.github.fommil.ff.Main;

//this class is written only for smoke test
public class mainTest {

	private Robot robot;

	@Before
	public void setUp() throws AWTException {
		robot = new Robot();
	}

	@Test
	public void playerWinSmoke() throws IOException, InterruptedException {
		// initiate game
		Main.main(null);

		Thread.sleep(3000);

		robot.keyPress(KeyEvent.VK_LEFT);
		Thread.sleep(1000);

		robot.keyRelease(KeyEvent.VK_LEFT);

		Thread.sleep(3000);

		robot.keyPress(KeyEvent.VK_DOWN);
		Thread.sleep(1900);

		robot.keyRelease(KeyEvent.VK_DOWN);

		Thread.sleep(3000);

		robot.keyPress(KeyEvent.VK_LEFT);
		Thread.sleep(5000);

		robot.keyRelease(KeyEvent.VK_LEFT);
		Thread.sleep(3000);
	}

}
