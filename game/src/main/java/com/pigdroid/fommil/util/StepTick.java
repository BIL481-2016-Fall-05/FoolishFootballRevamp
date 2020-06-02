package com.pigdroid.fommil.util;

public abstract class StepTick implements Tickable {

	private final long stepInMillis;
	private long accumulatedElapsed = 0L;

	protected StepTick(long stepInMillis) {
		this.stepInMillis = stepInMillis;
	}

	@Override
	public final void doTick(long elapsedTimeInMillis) {
		accumulatedElapsed += elapsedTimeInMillis;
		if (stepInMillis <= accumulatedElapsed) {
			doStep(stepInMillis);
			accumulatedElapsed = accumulatedElapsed % stepInMillis;
		}
	}

	protected abstract void doStep(long elapsedTimeInMillis);

}
