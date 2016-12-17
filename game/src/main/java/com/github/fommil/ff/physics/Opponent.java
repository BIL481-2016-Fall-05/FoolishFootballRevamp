package com.github.fommil.ff.physics;

import com.github.fommil.ff.PlayerStats;
import com.github.fommil.ff.Team;
import org.ode4j.ode.DSpace;
import org.ode4j.ode.DWorld;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * The model (M) for a opponent.
 *
 * @author Doga Can Yanikoglu
 */
public class Opponent extends Player {

	private final AssignAgent assignAgent;
	private final CheckAgent checkAgent;
	public ArrayList<Position> assignments = new ArrayList<Position>();
	private GamePhysics game;
	Semaphore s1;

	public Opponent(int i, Team team, PlayerStats stats, DWorld world, DSpace space, GamePhysics game) {
		super(i, team, stats, world, space);
		this.game = game;
        s1 = new Semaphore(0);
		assignAgent = new AssignAgent();
		assignAgent.start();
        checkAgent = new CheckAgent();
		checkAgent.start();
	}

	@Override
	double getAutoPilotTolerance() {
		return 0.1;
	}

	private class AssignAgent extends Thread {
		public void run() {
			while (true) {
                if (assignments.size() == 0) {
                    assignments.add(new Position(game.getSelected().getPosition().toDVector().add(-5, 5, 0)));
                    assignments.add(new Position(game.getSelected().getPosition().toDVector().add(5, 5, 0)));
                    assignments.add(new Position(game.getSelected().getPosition().toDVector().add(5, -5, 0)));
                    assignments.add(new Position(game.getSelected().getPosition().toDVector().add(-5, -5, 0)));
                    assignments.add(new Position(game.getSelected().getPosition().toDVector().add(-5, 5, 0)));
                } else {
                    try {
                        System.out.println("Acquired");
                        s1.acquire();
                    } catch (InterruptedException e) {

                    }
                }
            }
		}
	}

	private class CheckAgent extends Thread {
		public void run() {
			while (true) {
                if(assignments.size() != 0) {
                    if(Opponent.this.getPosition().distance(assignments.get(0)) < 1) {
                        assignments.remove(0);
                        s1.release();
                        System.out.println("Released");
                    }
                }
			}
		}
	}
}
